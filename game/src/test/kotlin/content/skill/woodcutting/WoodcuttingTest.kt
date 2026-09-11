package content.skill.woodcutting

import WorldTest
import containsMessage
import content.entity.player.bank.bank
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

internal class WoodcuttingTest : WorldTest() {

    @Test
    fun `Woodcutting gives log and depletes`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Woodcutting, 100)
        val tile = emptyTile.addY(1)
        val tree = createObject("tree", tile)
        player.inventory.add("bronze_hatchet")

        player.objectOption(tree, "Chop down")
        tickIf { player.inventory.spaces >= 27 }

        assertTrue(player.inventory.contains("logs"))
        assertTrue(player.experience.get(Skill.Woodcutting) > 0)
        assertNotEquals(tree.id, GameObjects.getLayer(tile, ObjectLayer.GROUND)?.id)
    }

    @Test
    fun `Evil tree magic escorts logs to the bank when the inventory is full`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Woodcutting, 100)
        player["evil_tree_buff"] = 600
        player.inventory.add("bronze_hatchet", 28)
        val tile = emptyTile.addY(1)
        val tree = createObject("tree", tile)

        player.objectOption(tree, "Chop down")
        tickIf { player.bank.count("logs") == 0 }

        assertTrue(player.bank.count("logs") > 0)
        assertTrue(player.inventory.isFull())
        assertTrue(player.containsMessage("The logs are magically escorted to your bank."))
    }

    @Test
    fun `A full inventory still stops woodcutting without evil tree magic`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Woodcutting, 100)
        player.inventory.add("bronze_hatchet", 28)
        val tile = emptyTile.addY(1)
        val tree = createObject("tree", tile)

        player.objectOption(tree, "Chop down")
        tick(10)

        assertEquals(0, player.bank.count("logs"))
        assertTrue(player.containsMessage("Your inventory is too full to hold any more logs."))
    }
}
