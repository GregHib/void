package content.area.wilderness

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MuddyChestTest : WorldTest() {

    @Test
    fun `Muddy chest can be opened and looted with key in inventory`() {
        val player = createPlayer(emptyTile)
        val tile = emptyTile.addY(1)
        val chest = createObject("wilderness_muddy_chest_closed", tile)
        player.inventory.add("muddy_key")

        player.objectOption(chest, "Open")
        tick(5)

        assertFalse(player.inventory.contains("muddy_key"))
        assertTrue(player.containsMessage("You unlock the chest with your key."))
        assertEquals(0, player.inventory.count("uncut_ruby"))
        GameObjects.find(tile, "wilderness_muddy_chest_closed")

        tick(1)

        GameObjects.find(tile, "wilderness_muddy_chest_open")
        assertTrue(player.containsMessage("You find some treasure in the chest!"))
        assertEquals(1, player.inventory.count("uncut_ruby"))
        assertEquals(1, player.inventory.count("mithril_bar"))
        assertEquals(1, player.inventory.count("mithril_dagger"))
        assertEquals(1, player.inventory.count("anchovy_pizza"))
        assertEquals(2, player.inventory.count("law_rune"))
        assertEquals(2, player.inventory.count("death_rune"))
        assertEquals(10, player.inventory.count("chaos_rune"))
        assertEquals(50, player.inventory.count("coins"))

        tick(1)
        GameObjects.find(tile, "wilderness_muddy_chest_closed")
    }

    @Test
    fun `Muddy chest stays locked without key`() {
        val player = createPlayer(emptyTile)
        val tile = emptyTile.addY(1)
        val chest = createObject("wilderness_muddy_chest_closed", tile)

        player.objectOption(chest, "Open")
        tick(2)

        GameObjects.find(tile, "wilderness_muddy_chest_closed")
    }
}
