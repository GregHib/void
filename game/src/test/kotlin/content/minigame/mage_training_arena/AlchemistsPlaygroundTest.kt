package content.minigame.mage_training_arena

import WorldTest
import containsMessage
import content.entity.player.bank.bank
import interfaceOnItem
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class AlchemistsPlaygroundTest : WorldTest() {

    @Test
    fun `Cupboards hold the item for their rotated slot`() {
        val player = enter("mta-alch-cupboard")
        val cupboard = MageTrainingArenaTest.findObject("cupboard_mage_training_arena_2", Tile(3341, 9618, 2), Tile(3391, 9654, 2))
        player.tele(cupboard.tile.addY(-1))
        tick(2)

        player.objectOption(cupboard, "Search")
        tick(5)

        assertTrue(player.inventory.contains("adamant_kiteshield_mage_training_arena"))
        assertTrue(player.containsMessage("You found: Adamant kiteshield"))
    }

    @Test
    fun `Alching an arena item makes arena coins worth its current value`() {
        val player = enter("mta-alch-cast")
        player.inventory.add("leather_boots_mage_training_arena")
        player.inventory.add("fire_rune", 3)
        player.inventory.add("nature_rune", 1)

        player.interfaceOnItem("modern_spellbook", "low_level_alchemy", Item("leather_boots_mage_training_arena"), player.inventory.indexOf("leather_boots_mage_training_arena"))
        tick(3)

        assertEquals(30, player.inventory.count("coins_mage_training_arena"))
        assertEquals(0, player.inventory.count("nature_rune"))
        assertEquals(31.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `The free item costs no runes`() {
        val player = enter("mta-alch-free")
        AlchemistsPlayground.freeItem = "emerald_mage_training_arena"
        player.inventory.add("emerald_mage_training_arena")

        player.interfaceOnItem("modern_spellbook", "low_level_alchemy", Item("emerald_mage_training_arena"), player.inventory.indexOf("emerald_mage_training_arena"))
        tick(3)

        assertEquals(8, player.inventory.count("coins_mage_training_arena"))
    }

    @Test
    fun `Normal items can't be alched in the playground`() {
        val player = enter("mta-alch-normal")
        player.inventory.add("bronze_sword")
        player.inventory.add("fire_rune", 3)
        player.inventory.add("nature_rune", 1)

        player.interfaceOnItem("modern_spellbook", "low_level_alchemy", Item("bronze_sword"), player.inventory.indexOf("bronze_sword"))
        tick(3)

        assertTrue(player.inventory.contains("bronze_sword"))
        assertTrue(player.containsMessage("You can only alch items from the cupboards!"))
    }

    @Test
    fun `Depositing coins gives points, experience and a reward share`() {
        val player = enter("mta-alch-deposit")
        player.inventory.add("coins_mage_training_arena", 250)
        val collector = MageTrainingArenaTest.findObject("coin_collector", Tile(3341, 9618, 2), Tile(3391, 9654, 2))
        player.tele(collector.tile.addY(-2))
        tick(2)

        player.objectOption(collector, "Deposit")
        tick(6)
        player.skipDialogues()

        assertEquals(0, player.inventory.count("coins_mage_training_arena"))
        assertEquals(2, PizazzPoints.get(player, "alchemist"))
        assertEquals(500.0, player.experience.get(Skill.Magic))
        assertEquals(20, player["mage_training_arena_alchemist_reward", 0])
    }

    @Test
    fun `Depositing too many coins ejects the player`() {
        val player = enter("mta-alch-eject")
        player.inventory.add("coins_mage_training_arena", 12000)
        val collector = MageTrainingArenaTest.findObject("coin_collector", Tile(3341, 9618, 2), Tile(3391, 9654, 2))
        player.tele(collector.tile.addY(-2))
        tick(2)

        player.objectOption(collector, "Deposit")
        tick(6)

        assertEquals(MageTrainingArena.lobby, player.tile)
        assertEquals(0, player.inventory.count("coins_mage_training_arena"))
        assertEquals(0, PizazzPoints.get(player, "alchemist"))
    }

    @Test
    fun `Leaving banks the reward and removes leftover coins`() {
        val player = enter("mta-alch-exit")
        player["mage_training_arena_alchemist_reward"] = 20
        player.inventory.add("coins_mage_training_arena", 50)
        val exit = MageTrainingArenaTest.findObject("exit_portal_mage_training_arena", Tile(3341, 9618, 2), Tile(3391, 9654, 2))
        player.tele(exit.tile.addY(-1))
        tick(2)

        player.objectOption(exit, "Enter")
        tick(10)

        assertEquals(Tile(3363, 3320), player.tile)
        assertEquals(0, player.inventory.count("coins_mage_training_arena"))
        assertEquals(20, player.bank.count("coins"))
        assertEquals(0, player["mage_training_arena_alchemist_reward", 0])
        assertTrue(player.containsMessage("You've been awarded 20 coins straight into your bank as a reward!"))
    }

    private fun enter(name: String): Player {
        val player = createPlayer(Tile(3361, 3316), name)
        player.levels.set(Skill.Magic, 55)
        player["mage_training_arena_started"] = true
        player.inventory.add("progress_hat")
        player.tele(Tile(3366, 9623, 2))
        tick(2)
        assertEquals("alchemist", player["mage_training_arena_room", ""])
        AlchemistsPlayground.rotation = 0
        AlchemistsPlayground.freeItem = null
        AlchemistsPlayground.values["leather_boots_mage_training_arena"] = 30
        AlchemistsPlayground.values["adamant_kiteshield_mage_training_arena"] = 15
        AlchemistsPlayground.values["adamant_helm_mage_training_arena"] = 5
        AlchemistsPlayground.values["emerald_mage_training_arena"] = 8
        AlchemistsPlayground.values["rune_longsword_mage_training_arena"] = 1
        return player
    }
}
