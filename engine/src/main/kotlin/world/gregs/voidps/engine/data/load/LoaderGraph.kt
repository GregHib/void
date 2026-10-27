package world.gregs.voidps.engine.data.load

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.snapshot.Snapshot
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * Runs [Loader]s in dependency order, in parallel where possible.
 * Each loader's snapshot is keyed on its inputs and the keys of everything it depends on,
 * so a change anywhere upstream invalidates every snapshot downstream.
 */
class LoaderGraph(loaders: List<Loader<*>>) {

    /** Topologically sorted; dependencies come before dependents. */
    val loaders: List<Loader<*>> = sort(loaders)

    /** Names of the loaders restored from a snapshot by the last run, mostly for tests. */
    val restored: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun get(name: String): Loader<*>? = loaders.firstOrNull { it.name == name }

    /**
     * The snapshot key of every loader, including those without a snapshot as others are keyed on them.
     * Null when the config files weren't scanned.
     */
    fun keys(files: ConfigFiles, setting: (String) -> String? = Settings::getOrNull): Map<Loader<*>, Long?> {
        val keys = LinkedHashMap<Loader<*>, Long?>()
        for (loader in loaders) {
            keys[loader] = key(loader, files, setting, keys)
        }
        return keys
    }

    private fun key(loader: Loader<*>, files: ConfigFiles, setting: (String) -> String?, keys: Map<Loader<*>, Long?>): Long? {
        if (files.stamps.isEmpty()) {
            return null
        }
        var key = MAGIC
        key = key * 31 + (loader.codec?.schema ?: 0L)
        key = key * 31 + loader.name.hashCode()
        for (path in LoadContext(files, null).paths(loader)) {
            key = key * 31 + path.hashCode()
            key = key * 31 + (files.stamps[path] ?: return null)
        }
        for (name in loader.settings) {
            key = key * 31 + name.hashCode()
            key = key * 31 + (setting(name) ?: "").hashCode()
        }
        if (loader.usesCache) {
            key = key * 31 + files.cacheModified
        }
        for (dependency in loader.dependsOn.sortedBy { it.name }) {
            key = key * 31 + (keys[dependency] ?: return null)
        }
        return key
    }

    /**
     * Loads and installs everything.
     * @param snapshots directory to read and write snapshots in, null to disable them.
     */
    fun run(files: ConfigFiles, cache: Cache?, snapshots: Path? = snapshotDirectory()): Map<Loader<*>, Any> = execute(loaders.toSet(), emptyMap(), files, cache, snapshots, restore = true)

    /**
     * Loads and installs [name] and everything that depends on it again, without restoring snapshots.
     * @param previous the results of the last run, used for loaders that aren't reloaded.
     * @return the names of every loader that was reloaded in install order, and the new results.
     */
    fun reload(name: String, files: ConfigFiles, cache: Cache?, previous: Map<Loader<*>, Any>, snapshots: Path? = snapshotDirectory()): Pair<List<String>, Map<Loader<*>, Any>> {
        val root = get(name) ?: throw IllegalArgumentException("No loader named '$name'.")
        val targets = mutableSetOf(root)
        for (loader in loaders) {
            if (loader.dependsOn.any { it in targets }) {
                targets.add(loader)
            }
        }
        val results = execute(targets, previous, files, cache, snapshots, restore = false)
        return loaders.filter { it in targets }.map { it.name } to results
    }

    /**
     * Installs [results] of a previous run again, in dependency order.
     */
    @Suppress("UNCHECKED_CAST")
    fun install(results: Map<Loader<*>, Any>) {
        for (loader in loaders) {
            (loader as Loader<Any>).install(results.getValue(loader))
        }
    }

    private fun execute(targets: Set<Loader<*>>, previous: Map<Loader<*>, Any>, files: ConfigFiles, cache: Cache?, snapshots: Path?, restore: Boolean): Map<Loader<*>, Any> {
        val keys = if (snapshots == null) emptyMap() else keys(files)
        restored.clear()
        return runBlocking(Dispatchers.Default) {
            val nodes = LinkedHashMap<Loader<*>, Deferred<Any>>()
            for (loader in loaders) {
                if (loader !in targets) {
                    nodes[loader] = CompletableDeferred(previous.getValue(loader))
                    continue
                }
                val dependencies = loader.dependsOn.map { it to nodes.getValue(it) }
                nodes[loader] = async {
                    val results = dependencies.associate { (dependency, deferred) -> dependency to deferred.await() }
                    run(loader, LoadContext(files, cache, results), snapshots, keys[loader], restore)
                }
            }
            nodes.mapValues { it.value.await() }
        }
    }

    private fun <T : Any> run(loader: Loader<T>, context: LoadContext, directory: Path?, key: Long?, restore: Boolean): T {
        val start = System.currentTimeMillis()
        val codec = loader.codec
        val snapshot = if (codec != null && directory != null && key != null) Snapshot(directory.resolve("${loader.name}.snapshot"), key) else null
        var state: T? = null
        if (restore && snapshot != null && codec != null) {
            state = snapshot.read { codec.read(it) }
        }
        if (state != null) {
            restored.add(loader.name)
        } else {
            val loaded = loader.load(context)
            if (snapshot != null && codec != null) {
                snapshot.write { codec.write(this, loaded) }
            }
            state = loaded
        }
        loader.install(state)
        logger.info { "Loaded ${loader.name} in ${System.currentTimeMillis() - start}ms${if (loader.name in restored) " (snapshot)" else ""}" }
        return state
    }

    companion object {
        private val logger = InlineLogger()
        private const val MAGIC = 0x564F4944L

        fun snapshotDirectory(): Path? = if (Settings["storage.snapshots.active", false]) Path.of(Settings["storage.caching.path"]) else null

        private fun sort(loaders: List<Loader<*>>): List<Loader<*>> {
            loaders.groupBy { it.name }.entries.firstOrNull { it.value.size > 1 }?.let { throw IllegalArgumentException("Duplicate loader name '${it.key}'.") }
            val registered = loaders.toSet()
            for (loader in loaders) {
                for (dependency in loader.dependsOn) {
                    require(dependency in registered) { "Loader '${loader.name}' depends on unregistered loader '${dependency.name}'." }
                }
            }
            val sorted = ArrayList<Loader<*>>(loaders.size)
            val done = HashSet<Loader<*>>()
            val path = ArrayList<Loader<*>>()
            fun visit(loader: Loader<*>) {
                if (loader in done) {
                    return
                }
                if (loader in path) {
                    val cycle = path.subList(path.indexOf(loader), path.size) + loader
                    throw IllegalArgumentException("Loader cycle: ${cycle.joinToString(" -> ") { it.name }}")
                }
                path.add(loader)
                for (dependency in loader.dependsOn) {
                    visit(dependency)
                }
                path.removeAt(path.lastIndex)
                done.add(loader)
                sorted.add(loader)
            }
            for (loader in loaders) {
                visit(loader)
            }
            return sorted
        }
    }
}
