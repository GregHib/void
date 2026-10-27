package world.gregs.voidps.engine.data.load

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.snapshot.SnapshotReader
import world.gregs.voidps.engine.data.snapshot.SnapshotWriter

/**
 * Loads one kind of definition or config from the cache and/or config files.
 * [load] builds a new state without touching any global, [install] publishes it.
 */
interface Loader<T : Any> {
    /** Unique; used as the snapshot file name and reload alias. */
    val name: String

    /** Setting keys whose file lists are inputs, e.g. "definitions.items". Resolved with [ConfigFiles.list]. */
    val files: List<String> get() = emptyList()

    /** Setting keys of single file inputs. Resolved with [ConfigFiles.find]. */
    val file: List<String> get() = emptyList()

    val dependsOn: List<Loader<*>> get() = emptyList()

    /** Only the settings that change this loader's output, e.g. "world.members". */
    val settings: List<String> get() = emptyList()

    /** True if the output depends on the RS cache, so when it was modified is part of the key. */
    val usesCache: Boolean get() = false

    /** Null means never snapshot, for loaders that are cheap enough to always parse. */
    val codec: SnapshotCodec<T>? get() = null

    /** Builds a NEW state. Must not mutate globals or previously installed state. */
    fun load(context: LoadContext): T

    /** Publishes [state]; a plain field swap on the singleton. */
    fun install(state: T)
}

interface SnapshotCodec<T> {
    fun write(writer: SnapshotWriter, value: T)

    fun read(reader: SnapshotReader): T

    /** Folded into the key so changing the shape of the data invalidates old snapshots. */
    val schema: Long
}

class LoadContext(
    val files: ConfigFiles,
    val cache: Cache?,
    private val dependencies: Map<Loader<*>, Any> = emptyMap(),
    /** The snapshot key of the loader being run, which changes whenever anything it reads does. Null if unknown. */
    val key: Long? = null,
) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(loader: Loader<T>): T = dependencies[loader] as? T ?: throw IllegalStateException("Loader '${loader.name}' is not a declared dependency.")

    /** The input files of [loader] */
    fun paths(loader: Loader<*>): List<String> = loader.files.flatMap { files.list(Settings[it]) } + loader.file.map { files.find(Settings[it]) }

    fun list(key: String): List<String> = files.list(Settings[key])

    fun find(key: String): String = files.find(Settings[key])

    fun requireCache(): Cache = cache ?: throw IllegalStateException("A cache is required.")
}
