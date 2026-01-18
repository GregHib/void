package content.area.kharidian_desert.shantay_pass

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class ShantayPassTest : WorldTest() {

    @Test
    fun `Can't enter desert without pass`() {
        val player = createPlayer(Tile(3305, 3117))
        val pass = GameObjects.find(Tile(3303, 3116), "shantay_pass")
        player.objectOption(pass, "Go-through")
        tick()
        assertNotNull(player.dialogue)
        assertEquals(Tile(3305, 3117), player.tile)
    }

    @Test
    fun `Enter through pass`() {
        val player = createPlayer(Tile(3305, 3117))
        player.inventory.add("shantay_pass")
        val pass = GameObjects.find(Tile(3303, 3116), "shantay_pass")
        player.objectOption(pass, "Go-through")
        tick(1)
        Dialogues.continueDialogue(player, "warning_shantay_pass:yes")
        tick(3)
        assertEquals(Tile(3305, 3116), player.tile)
    }

    @Test
    fun `Exit through pass`() {
        val player = createPlayer(Tile(3305, 3115))
        player.inventory.add("shantay_pass")
        val pass = GameObjects.find(Tile(3303, 3116), "shantay_pass")
        player.objectOption(pass, "Go-through")
        tick(3)
        assertEquals(Tile(3305, 3117), player.tile)
    }
}
