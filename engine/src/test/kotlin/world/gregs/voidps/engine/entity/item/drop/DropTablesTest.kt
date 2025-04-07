package world.gregs.voidps.engine.entity.item.drop

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DropTablesTest {

    @Test
    fun `Load from toml`() {
        val uri = DropTablesTest::class.java.getResource("drop-table.toml")!!.toURI()
        val decoder = DropTables().load(listOf(uri.path))
        val table = decoder.getValue("test_drop_table")
        assertNotNull(table)
        assertNull(decoder.get("invalid_drop_table"))

        assertEquals(TableType.All, table.type)
        assertEquals(1, table.chance)
        val drops = table.drops
        val bones = drops.first() as ItemDrop
        assertEquals("bones", bones.id)
        assertEquals(1..1, bones.amount)

        val subTable = drops.last() as DropTable
        assertEquals(TableType.First, subTable.type)
        assertEquals(1, subTable.chance)
        val subDrops = subTable.drops
        var drop = subDrops[0] as ItemDrop
        assertEquals("coins", drop.id)
        assertEquals(10..10, drop.amount)
        assertEquals(2, drop.chance)
        assertNull(drop.predicate)

        drop = subDrops[1] as ItemDrop
        assertEquals("coins", drop.id)
        assertEquals(1..1, drop.amount)
        assertEquals(1, drop.chance)
        assertNull(drop.predicate)

        drop = subDrops[2] as ItemDrop
        assertEquals("air_rune", drop.id)
        assertEquals(1..1, drop.amount)
        assertEquals(1, drop.chance)
        assertNotNull(drop.predicate)
        Settings.load(mapOf("world.members" to "true"))
        assertFalse(drop.predicate!!.invoke(Player()))
        Settings.load(mapOf("world.members" to "false"))
        assertTrue(drop.predicate!!.invoke(Player()))

        drop = subDrops[3] as ItemDrop
        assertEquals("dragon_dagger", drop.id)
        assertEquals(1..1, drop.amount)
        assertEquals(1, drop.chance)
        assertNotNull(drop.predicate)
        assertFalse(drop.predicate!!.invoke(Player()))
        Settings.load(mapOf("world.members" to "true"))
        assertTrue(drop.predicate!!.invoke(Player()))

        drop = subDrops[4] as ItemDrop
        assertEquals("coins", drop.id)
        assertEquals(1..5, drop.amount)
        assertEquals(1, drop.chance)
        assertNull(drop.predicate)
    }
}