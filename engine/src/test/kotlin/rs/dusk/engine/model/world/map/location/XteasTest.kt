package rs.dusk.engine.model.world.map.location

import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.File
import java.io.RandomAccessFile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 17, 2020
 */
internal class XteasTest {

    @Test
    fun `Load from text file`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.location.XteasKt")
        val regionId = 12342
        val keys = intArrayOf(733680141, -1440926564, 447905675, 1806603117)
        val file = File("./$regionId.txt")
        try {
            file.writeText(keys.joinToString(separator = "\n"))
            // When
            val xteas = loadXteas("./", "", "")
            // Then
            assertEquals(1, xteas.size)
            val xtea = xteas[regionId]
            assertNotNull(xtea)
            assert(xtea!!.contentEquals(keys))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `Load from json`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.location.XteasKt")
        val file = File("./xteas.json")
        val regionId = 12342
        val keys = intArrayOf(733680141, -1440926564, 447905675, 1806603117)
        val id = "mapsquare"
        val value = "key"
        try {
            file.writeText(
                """
                [
                  {
                    "$id": $regionId,
                    "$value": [
                        ${keys.joinToString(separator = ",\n")}
                    ]
                  }
                ]
            """.trimIndent()
            )
            // When
            val xteas = loadXteas("./xteas.json", id, value)
            // Then
            assertEquals(1, xteas.size)
            val xtea = xteas[regionId]
            assertNotNull(xtea)
            assert(xtea!!.contentEquals(keys))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `Load from byte data`() {
        // Given
        mockkStatic("rs.dusk.engine.model.world.map.location.XteasKt")
        val file = File("./xteas.dat")
        val regionId = 12342
        val keys = intArrayOf(733680141, -1440926564, 447905675, 1806603117)
        try {
            val raf = RandomAccessFile(file, "rw")
            raf.writeShort(regionId)
            keys.forEach {
                raf.writeInt(it)
            }
            // When
            val xteas = loadXteas("./xteas.dat", "", "")
            // Then
            assertEquals(1, xteas.size)
            val xtea = xteas[regionId]
            assertNotNull(xtea)
            assert(xtea!!.contentEquals(keys))
        } finally {
            file.delete()
        }
    }
}