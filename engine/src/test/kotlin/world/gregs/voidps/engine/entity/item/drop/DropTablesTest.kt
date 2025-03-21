package world.gregs.voidps.engine.entity.item.drop

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DropTablesTest {

    @Test
    fun `Load from toml`() {
        val uri = DropTablesTest::class.java.getResource("drop-table.toml")!!.toURI()
        val decoder = DropTables().load(uri.path)
        val table = decoder.getValue("test_drop_table")
        assertNotNull(table)
        assertNull(decoder.get("invalid_drop_table"))
    }
}