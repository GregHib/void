package world.gregs.voidps.cache.definition.types

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import java.io.File

class DropTableTypesTest {

    @TempDir
    lateinit var dir: File

    lateinit var file: File

    @BeforeEach
    fun setup() {
        file = dir.resolve("test.drops.toml")
        file.writeText("""
            [some_drop]
            roll = 10
            type = "all"
            drops = [
                { id = "nature_talisman", chance = 2 },
                { id = "elemental_talisman" },
                { id = "small_pouch", chance = 6, lacks = "small_pouch" },
                { id = "medium_pouch", chance = 6, charges = 45, owns = "small_pouch", lacks = "medium_pouch*" },
            ]
        """.trimIndent())
    }

    @Test
    fun `Load from file`() {
        DropTableTypes.set(5)
        DropTableTypes.load(listOf(file.absolutePath))

        assertEquals(0, DropTableTypes.get("some_drop"))
        assertEquals(0, DropTableTypes.get("some_drop"))

        assertEquals(10, DropTableTypes.roll(0))
        assertTrue(DropTableTypes.dropAll(0))
        assertArrayEquals(intArrayOf(1, 2, 3, 4), DropTableTypes.drops(0))

        assertEquals("nature_talisman", DropTableTypes.item(1))
        assertEquals(1, DropTableTypes.min(1))
        assertEquals(1, DropTableTypes.max(1))
        assertEquals(2, DropTableTypes.chance(1))

        assertEquals("elemental_talisman", DropTableTypes.item(2))
        assertEquals(-1, DropTableTypes.chance(2))

        assertEquals("small_pouch", DropTableTypes.item(3))
        assertEquals("small_pouch", DropTableTypes.lacks(3))
        assertEquals(6, DropTableTypes.chance(3))

        assertEquals("medium_pouch", DropTableTypes.item(4))
        assertEquals(6, DropTableTypes.chance(4))
        assertEquals(45, DropTableTypes.min(4))
        assertEquals(45, DropTableTypes.max(4))
        assertEquals("small_pouch", DropTableTypes.owns(4))
        assertEquals("medium_pouch*", DropTableTypes.lacks(4))
    }

    @Test
    fun `Data is persisted`() {
        DropTableTypes.set(5)
        DropTableTypes.load(listOf(file.absolutePath))
        val writer = ArrayWriter(500_000)

        DropTableTypes.save(writer)

        val anys = DropTableTypes.nullAnys()
        val shorts = DropTableTypes.shorts()
        val ints = DropTableTypes.ints()
        val strings = DropTableTypes.nullStrings()
        val bytes = DropTableTypes.bytes()
        val intArrays = DropTableTypes.nullIntArrays()

        val reader = ArrayReader(writer.toArray())

        DropTableTypes.set(DropTableTypes.index)
        DropTableTypes.load(reader)

        for (i in anys.indices) {
            assertArrayEquals(anys[i], DropTableTypes.nullAnys()[i])
        }
        for (i in shorts.indices) {
            assertArrayEquals(shorts[i], DropTableTypes.shorts()[i])
        }
        for (i in ints.indices) {
            assertArrayEquals(ints[i], DropTableTypes.ints()[i])
        }
        for (i in strings.indices) {
            assertArrayEquals(strings[i], DropTableTypes.nullStrings()[i])
        }
        for (i in bytes.indices) {
            assertArrayEquals(bytes[i], DropTableTypes.bytes()[i])
        }
        for (i in intArrays.indices) {
            assertArrayEquals(intArrays[i], DropTableTypes.nullIntArrays()[i])
        }
    }

}