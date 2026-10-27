package world.gregs.voidps.engine.data.load

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.snapshot.SnapshotReader
import world.gregs.voidps.engine.data.snapshot.SnapshotWriter
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue

class LoaderGraphTest {

    @TempDir
    lateinit var temp: Path

    private val loads = ConcurrentLinkedQueue<String>()
    private val installs = ConcurrentLinkedQueue<String>()

    private class Intcodec : SnapshotCodec<Int> {
        override val schema = 1L
        override fun write(writer: SnapshotWriter, value: Int) = writer.int(value)
        override fun read(reader: SnapshotReader) = reader.int()
    }

    private inner class TestLoader(
        override val name: String,
        override val dependsOn: List<Loader<*>> = emptyList(),
        override val files: List<String> = emptyList(),
        override val settings: List<String> = emptyList(),
        val value: Int = 1,
        val read: Loader<*>? = null,
    ) : Loader<Int> {
        override val codec: SnapshotCodec<Int> = Intcodec()
        override fun load(context: LoadContext): Int {
            loads.add(name)
            read?.let { context[it as Loader<Int>] }
            return value + dependsOn.sumOf { context[it as Loader<Int>] }
        }

        override fun install(state: Int) {
            installs.add(name)
        }
    }

    @BeforeEach
    fun setup() {
        Settings.load(mapOf("defs.a" to "a.toml", "defs.b" to "b.toml", "members" to "true"))
    }

    @AfterEach
    fun teardown() {
        Settings.clear()
    }

    private fun files(a: Long = 1L, b: Long = 1L) = ConfigFiles(
        mapOf("a.toml" to listOf("/data/a.toml"), "b.toml" to listOf("/data/b.toml")),
        stamps = mapOf("/data/a.toml" to a, "/data/b.toml" to b),
    )

    @Test
    fun `Loaders are sorted so dependencies come first`() {
        val a = TestLoader("a")
        val b = TestLoader("b", dependsOn = listOf(a))
        val c = TestLoader("c", dependsOn = listOf(b, a))
        val graph = LoaderGraph(listOf(c, b, a))
        assertEquals(listOf("a", "b", "c"), graph.loaders.map { it.name })
    }

    @Test
    fun `Cycles are reported with their path`() {
        val a = object : Loader<Int> {
            override val name = "a"
            override val dependsOn: List<Loader<*>> get() = listOf(b)
            override fun load(context: LoadContext) = 1
            override fun install(state: Int) {}
        }
        val c = object : Loader<Int> {
            override val name = "c"
            override val dependsOn: List<Loader<*>> get() = listOf(a)
            override fun load(context: LoadContext) = 1
            override fun install(state: Int) {}
        }
        val d = object : Loader<Int> {
            override val name = "b"
            override val dependsOn: List<Loader<*>> get() = listOf(c)
            override fun load(context: LoadContext) = 1
            override fun install(state: Int) {}
        }
        b = d
        val exception = assertThrows<IllegalArgumentException> { LoaderGraph(listOf(a, c, d)) }
        assertEquals("Loader cycle: a -> b -> c -> a", exception.message)
    }

    private lateinit var b: Loader<Int>

    @Test
    fun `Duplicate names and unregistered dependencies are rejected`() {
        assertThrows<IllegalArgumentException> { LoaderGraph(listOf(TestLoader("a"), TestLoader("a"))) }
        val a = TestLoader("a")
        assertThrows<IllegalArgumentException> { LoaderGraph(listOf(TestLoader("b", dependsOn = listOf(a)))) }
    }

    @Test
    fun `Reading an undeclared dependency throws`() {
        val a = TestLoader("a")
        val b = TestLoader("b", read = a)
        val graph = LoaderGraph(listOf(a, b))
        assertThrows<IllegalStateException> { graph.run(files(), null, null) }
    }

    @Test
    fun `Dependencies are available to dependents and installed first`() {
        val a = TestLoader("a", value = 5)
        val b = TestLoader("b", dependsOn = listOf(a), value = 1)
        val results = LoaderGraph(listOf(b, a)).run(files(), null, null)
        assertEquals(5, results[a])
        assertEquals(6, results[b])
        assertEquals(listOf("a", "b"), installs.toList())
    }

    @Test
    fun `Changing an upstream file only changes downstream keys`() {
        val a = TestLoader("a", files = listOf("defs.a"))
        val b = TestLoader("b", dependsOn = listOf(a), files = listOf("defs.b"))
        val c = TestLoader("c", files = listOf("defs.b"))
        val graph = LoaderGraph(listOf(a, b, c))
        val before = graph.keys(files())
        val after = graph.keys(files(a = 2L))
        assertNotEquals(before[a], after[a])
        assertNotEquals(before[b], after[b])
        assertEquals(before[c], after[c])
    }

    @Test
    fun `Changing a setting only changes loaders that declare it`() {
        val a = TestLoader("a", settings = listOf("members"))
        val b = TestLoader("b")
        val c = TestLoader("c", dependsOn = listOf(a))
        val graph = LoaderGraph(listOf(a, b, c))
        val before = graph.keys(files())
        val after = graph.keys(files()) { if (it == "members") "false" else null }
        assertNotEquals(before[a], after[a])
        assertEquals(before[b], after[b])
        assertNotEquals(before[c], after[c])
    }

    @Test
    fun `Loaders without stamps can't be snapshotted`() {
        val a = TestLoader("a")
        assertEquals(null, LoaderGraph(listOf(a)).keys(ConfigFiles(emptyMap()))[a])
    }

    @Test
    fun `Snapshots are restored when the key matches and loaded when it doesn't`() {
        val a = TestLoader("a", files = listOf("defs.a"), value = 3)
        val b = TestLoader("b", dependsOn = listOf(a), value = 1)
        val graph = LoaderGraph(listOf(a, b))
        graph.run(files(), null, temp)
        assertEquals(listOf("a", "b"), loads.toList())
        assertTrue(temp.resolve("a.snapshot").toFile().exists())

        loads.clear()
        val results = graph.run(files(), null, temp)
        assertEquals(emptyList<String>(), loads.toList())
        assertEquals(4, results[b])

        graph.run(files(a = 9L), null, temp)
        assertEquals(listOf("a", "b"), loads.sorted())
    }

    @Test
    fun `Reload reruns the loader and its dependents only`() {
        val a = TestLoader("a", value = 1)
        val b = TestLoader("b", dependsOn = listOf(a))
        val c = TestLoader("c", dependsOn = listOf(b))
        val d = TestLoader("d")
        val graph = LoaderGraph(listOf(a, b, c, d))
        val results = graph.run(files(), null, null)
        loads.clear()
        installs.clear()
        val (reloaded, _) = graph.reload("b", files(), null, results, null)
        assertEquals(listOf("b", "c"), reloaded)
        assertEquals(listOf("b", "c"), loads.toList())
        assertEquals(listOf("b", "c"), installs.toList())
    }
}
