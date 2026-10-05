package content.minigame.mage_training_arena

import WorldTest
import containsMessage
import interfaceOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class MageTrainingArenaRewardsTest : WorldTest() {

    @Test
    fun `Buying deducts points from every room`() {
        val player = open("mta-reward-buy", 30, 30, 300, 30)

        player.interfaceOption("mage_training_arena_rewards", "stock", "Buy", item = Item("beginner_wand"), slot = 0)
        tick(1)

        assertTrue(player.inventory.contains("beginner_wand"))
        assertEquals(0, PizazzPoints.total(player))
    }

    @Test
    fun `Can't afford without enough points in each room`() {
        val player = open("mta-reward-poor", 30, 30, 299, 30)

        player.interfaceOption("mage_training_arena_rewards", "stock", "Buy", item = Item("beginner_wand"), slot = 0)
        tick(1)

        assertFalse(player.inventory.contains("beginner_wand"))
        assertTrue(player.containsMessage("You cannot afford that item."))
        assertEquals(389, PizazzPoints.total(player))
    }

    @Test
    fun `Wand upgrades consume the previous wand`() {
        val player = open("mta-reward-wand", 60, 60, 600, 60)
        player.equipment.add("beginner_wand")

        player.interfaceOption("mage_training_arena_rewards", "stock", "Buy", item = Item("apprentice_wand"), slot = 1)
        tick(1)

        assertTrue(player.inventory.contains("apprentice_wand"))
        assertFalse(player.equipment.contains("beginner_wand"))
        assertEquals(0, PizazzPoints.total(player))
    }

    @Test
    fun `Wand upgrades need the previous wand`() {
        val player = open("mta-reward-no-wand", 60, 60, 600, 60)

        player.interfaceOption("mage_training_arena_rewards", "stock", "Buy", item = Item("apprentice_wand"), slot = 1)
        tick(1)

        assertFalse(player.inventory.contains("apprentice_wand"))
        assertTrue(player.containsMessage("You don't have the required wand to buy this upgrade."))
    }

    @Test
    fun `Bones to Peaches is learnt once`() {
        val player = open("mta-reward-peaches", 400, 600, 4000, 400)

        player.interfaceOption("mage_training_arena_rewards", "stock", "Buy", item = Item("bones_to_peaches_spell"), slot = 10)
        tick(1)
        player.skipDialogues()

        assertTrue(player["bones_to_peaches", false])
        assertFalse(player.inventory.contains("bones_to_peaches_spell"))
        assertEquals(200, PizazzPoints.get(player, "telekinetic"))
        assertEquals(300, PizazzPoints.get(player, "alchemist"))

        player.interfaceOption("mage_training_arena_rewards", "stock", "Buy", item = Item("bones_to_peaches_spell"), slot = 10)
        tick(1)

        assertTrue(player.containsMessage("You already unlocked that spell."))
        assertEquals(200, PizazzPoints.get(player, "telekinetic"))
    }

    @Test
    fun `The arena book costs coins`() {
        val player = open("mta-reward-book", 0, 0, 0, 0)
        player.inventory.add("coins", 250)

        player.interfaceOption("mage_training_arena_rewards", "stock", "Buy", item = Item("arena_book"), slot = 25)
        tick(1)

        assertTrue(player.inventory.contains("arena_book"))
        assertEquals(50, player.inventory.count("coins"))
    }

    @Test
    fun `Value lists the cost per room`() {
        val player = open("mta-reward-value", 0, 0, 0, 0)

        player.interfaceOption("mage_training_arena_rewards", "stock", "Value", item = Item("master_wand"), slot = 3)
        tick(1)

        assertTrue(player.containsMessage("The Master wand costs 240 Telekinetic, 240 Alchemist,"))
        assertTrue(player.containsMessage("2400 Enchantment and 240 Graveyard Pizazz Points."))
    }

    private fun open(name: String, telekinetic: Int, alchemist: Int, enchanting: Int, graveyard: Int): Player {
        val player = createPlayer(Tile(3362, 3319, 1), name)
        player["mage_training_arena_started"] = true
        player["mage_training_arena_telekinetic_points"] = telekinetic
        player["mage_training_arena_alchemist_points"] = alchemist
        player["mage_training_arena_enchanting_points"] = enchanting
        player["mage_training_arena_graveyard_points"] = graveyard
        val guardian = createNPC("rewards_guardian", Tile(3362, 3318, 1))
        player.npcOption(guardian, "Trade-with")
        tick(2)
        assertTrue(player.hasOpen("mage_training_arena_rewards"))
        return player
    }
}
