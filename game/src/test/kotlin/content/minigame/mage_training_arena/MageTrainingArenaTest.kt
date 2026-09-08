package content.minigame.mage_training_arena

import WorldTest
import containsMessage
import dialogueOption
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

internal class MageTrainingArenaTest : WorldTest() {

    @Test
    fun `Entrance guardian hands out a progress hat`() {
        val player = createPlayer(Tile(3363, 3305), "mta-hat")
        val guardian = createNPC("entrance_guardian", Tile(3363, 3304))

        player.npcOption(guardian, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()

        assertTrue(player["mage_training_arena_started", false])
        assertTrue(player.inventory.contains("progress_hat"))
        assertEquals("progress_hat", PizazzHat.tier(player))
    }

    @Test
    fun `Portals refuse players without a progress hat`() {
        val player = createPlayer(Tile(3361, 3316), "mta-no-hat")
        player.levels.set(Skill.Magic, 50)
        val portal = findObject("enchanters_portal", Tile(3355, 3310), Tile(3372, 3325))

        player.objectOption(portal, "Enter")
        tick(3)

        assertEquals(0, player.tile.level)
        assertTrue(player.tile.y < 3330)
        assertFalse(player.contains("mage_training_arena_room"))
    }

    @Test
    fun `Portals take hat holders into the room and open the overlay`() {
        val player = createPlayer(Tile(3361, 3316), "mta-enter")
        player.levels.set(Skill.Magic, 50)
        player["mage_training_arena_started"] = true
        player.inventory.add("progress_hat")
        val portal = findObject("enchanters_portal", Tile(3355, 3310), Tile(3372, 3325))

        player.objectOption(portal, "Enter")
        tick(3)

        assertEquals(Tile(3363, 9649), player.tile)
        assertEquals("enchanting", player["mage_training_arena_room", ""])
        assertTrue(player.hasOpen("mage_training_arena_enchanting"))
        assertTrue(player.containsMessage("You've entered the Enchanting Chamber."))
    }

    @Test
    fun `Exit portal returns to the lobby and confiscates arena items`() {
        val player = enter("mta-exit", "enchanting", Tile(3363, 9649))
        player.inventory.add("cube", 3)
        val exit = findObject("exit_portal_mage_training_arena", Tile(3335, 9612), Tile(3389, 9663))

        player.objectOption(exit, "Enter")
        tick(10)

        assertEquals(Tile(3361, 3318), player.tile)
        assertFalse(player.inventory.contains("cube"))
        assertFalse(player.contains("mage_training_arena_room"))
        assertFalse(player.hasOpen("mage_training_arena_enchanting"))
    }

    @Test
    fun `Teleport spells are blocked inside the arena`() {
        val player = enter("mta-teleport", "enchanting", Tile(3363, 9649))

        Teleport.teleport(player, Tile(3212, 3424), "modern", "varrock_teleport")
        tick(5)

        assertEquals(Tile(3363, 9649), player.tile)
        assertTrue(player.containsMessage("You can't teleport out of the training arena!"))
    }

    @Test
    fun `Pizazz points upgrade the hat tier`() {
        val player = createPlayer(Tile(3363, 3305), "mta-tier")
        player.inventory.add("progress_hat")

        PizazzPoints.add(player, "enchanting", 301)

        assertTrue(player.inventory.contains("progress_hat_2"))
        assertFalse(player.inventory.contains("progress_hat"))

        PizazzPoints.add(player, "graveyard", 300)

        assertTrue(player.inventory.contains("progress_hat_3"))
        assertEquals(601, PizazzPoints.total(player))
    }

    @Test
    fun `The hat can be talked to while worn`() {
        val player = createPlayer(Tile(3363, 3305), "mta-worn-hat")
        player.equipment.add("progress_hat")

        player.itemOption("Talk-to", "progress_hat", id = "worn_equipment", component = "hat_slot", optionIndex = 1, inventory = "worn_equipment", slot = EquipSlot.Hat.index)
        tick(1)

        assertNotNull(player.dialogue)
        player.skipDialogues()
        assertNull(player.dialogue)
    }

    private fun enter(name: String, room: String, tile: Tile): Player {
        val player = createPlayer(Tile(3361, 3316), name)
        player.levels.set(Skill.Magic, 50)
        player["mage_training_arena_started"] = true
        player.inventory.add("progress_hat")
        player.tele(tile)
        tick(2)
        assertEquals(room, player["mage_training_arena_room", ""])
        return player
    }

    companion object {
        fun findObject(id: String, from: Tile, to: Tile): GameObject {
            for (x in from.x..to.x) {
                for (y in from.y..to.y) {
                    val obj = GameObjects.findOrNull(Tile(x, y, from.level), id) ?: continue
                    return obj
                }
            }
            error("Object $id not found between $from and $to")
        }
    }
}
