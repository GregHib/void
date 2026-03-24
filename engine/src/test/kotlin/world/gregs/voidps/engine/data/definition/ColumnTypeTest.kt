package world.gregs.voidps.engine.data.definition

import org.junit.jupiter.api.Test
import world.gregs.config.Config
import kotlin.test.assertEquals

class ColumnTypeTest {

    @Test
    fun `Pair type`() {
        val type = ColumnReader.ReaderPair(ColumnReader.ReaderInt, ColumnReader.ReaderInt)
        Config.stringReader("[1, 4]") {
            assertEquals(Pair(1, 4), type.read(this))
        }
    }

    @Test
    fun `List type`() {
        val type = ColumnReader.ReaderList(ColumnReader.ReaderInt)
        Config.stringReader("[1, 2, 3]") {
            assertEquals(listOf(1, 2, 3), type.read(this))
        }
    }

    @Test
    fun `Pair list type`() {
        val type = ColumnReader.ReaderList(ColumnReader.ReaderPair(ColumnReader.ReaderString, ColumnReader.ReaderInt))
        Config.stringReader("[[\"test\", 3]]") {
            assertEquals(listOf(Pair("test", 3)), type.read(this))
        }
    }
}