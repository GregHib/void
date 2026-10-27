import gameLoaders
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.MemoryCache
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ItemLoader
import world.gregs.voidps.engine.data.definition.MapLoader
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.LoaderGraph
import world.gregs.voidps.engine.data.load.SnapshotCodec
import world.gregs.voidps.engine.data.snapshot.SnapshotReader
import world.gregs.voidps.engine.data.snapshot.SnapshotWriter
import world.gregs.voidps.engine.engineLoaders
import java.nio.file.Path
import java.util.Properties
import kotlin.test.assertEquals

/**
 * Prints how long every loader takes cold and restored from a snapshot, and how the snapshot codecs compare.
 * Skipped unless the environment variable `LOADER_BENCHMARK` is `true`:
 * `LOADER_BENCHMARK=true ./gradlew :game:test --tests LoaderBenchmark -i`
 */
@EnabledIfEnvironmentVariable(named = "LOADER_BENCHMARK", matches = "true")
internal class LoaderBenchmark {

    @TempDir
    lateinit var temp: Path

    private lateinit var files: ConfigFiles
    private lateinit var cache: Cache

    @BeforeEach
    fun setup() {
        val properties = Properties()
        properties.load(LoaderBenchmark::class.java.getResourceAsStream("/game.properties")!!)
        for ((key, value) in properties) {
            if (value is String && value.startsWith("./")) {
                properties[key] = value.replace("./", "../")
            }
        }
        properties["storage.caching.path"] = "$temp/"
        properties["storage.snapshots.active"] = "true"
        properties["world.members"] = "true"
        Settings.load(properties)
        files = configFiles()
        cache = MemoryCache(Settings["storage.cache.path"])
    }

    @AfterEach
    fun teardown() {
        Settings.clear()
    }

    @Test
    fun `Graph cold and warm`() {
        val graph = LoaderGraph((engineLoaders() + gameLoaders()).filter { it != MapLoader })
        // Class loading and JIT are part of a real start, but not of the runs after the first
        val start = System.nanoTime()
        graph.run(files, cache, temp)
        val cold = (System.nanoTime() - start) / 1_000_000
        val coldTimings = graph.timings.toMap()
        val runs = 10
        val warm = ArrayList<Long>()
        val timings = HashMap<String, MutableList<Long>>()
        repeat(runs) {
            val begin = System.nanoTime()
            graph.run(files, cache, temp)
            warm.add((System.nanoTime() - begin) / 1_000_000)
            for ((name, time) in graph.timings) {
                timings.getOrPut(name) { ArrayList() }.add(time)
            }
        }
        println("Loader benchmark: cold total ${cold}ms, warm median total ${warm.sorted()[runs / 2]}ms")
        for ((name, times) in timings.entries.sortedByDescending { it.value.sorted()[runs / 2] }) {
            println("  %-20s cold %5dms warm median %5dms".format(name, coldTimings[name], times.sorted()[runs / 2]))
        }
    }

    @Test
    fun `Item snapshot codec`() {
        val state = ItemLoader.load(LoadContext(files, cache))
        val bytes = write(ItemLoader.codec!!, state)
        val restored = ItemLoader.codec!!.read(SnapshotReader(bytes))
        assertEquals(state.definitions.toList(), restored.definitions.toList())
        assertEquals(state.ids.toSortedMap(), restored.ids.toSortedMap())
        repeat(40) { ItemLoader.codec!!.read(SnapshotReader(bytes)) }
        val times = List(100) {
            val start = System.nanoTime()
            ItemLoader.codec!!.read(SnapshotReader(bytes))
            (System.nanoTime() - start) / 1000
        }.sorted()
        println("Item codec: ${bytes.size} bytes, restore median ${times[50]}us min ${times[0]}us")
    }

    private fun <T> write(codec: SnapshotCodec<T>, state: T): ByteArray {
        val writer = SnapshotWriter()
        codec.write(writer, state)
        return writer.toByteArray()
    }
}
