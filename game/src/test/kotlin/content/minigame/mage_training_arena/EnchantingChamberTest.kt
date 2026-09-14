package content.minigame.mage_training_arena

import WorldTest
import containsMessage
import interfaceOnItem
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class EnchantingChamberTest : WorldTest() {

    @Test
    fun `Entering spawns private dragonstones and shows the bonus shape`() {
        val player = enter("mta-ench-enter")

        assertEquals(6, EnchantingChamber.dragonstones(player).size)
        val stone = FloorItems.at(Tile(3354, 9645)).firstOrNull { it.id == "dragonstone_mage_training_arena" }
        assertEquals(player.name, stone?.owner)
    }

    @Test
    fun `Shapes are taken from piles`() {
        val player = enter("mta-ench-pile")
        val pile = MageTrainingArenaTest.findObject("cube_pile", Tile(3335, 9612), Tile(3389, 9663))
        player.tele(pile.tile.addY(-1))
        tick(2)

        player.objectOption(pile, "Take-from")
        tick(5)

        assertTrue(player.inventory.contains("cube"))
    }

    @Test
    fun `Enchanting a shape makes an orb with reduced experience`() {
        val player = enter("mta-ench-cast")
        EnchantingChamber.bonus = "pentamid"
        player.inventory.add("cube")
        player.inventory.add("cosmic_rune")
        player.inventory.add("water_rune")

        enchant(player, "cube")

        assertTrue(player.inventory.contains("orb"))
        assertEquals(0, player.inventory.count("cosmic_rune"))
        assertEquals(13.1, player.experience.get(Skill.Magic))
        assertEquals(1, player["mage_training_arena_shapes_converted", 0])
        assertEquals(0, PizazzPoints.get(player, "enchanting"))
    }

    @Test
    fun `Every tenth shape scores the spell level`() {
        val player = enter("mta-ench-tenth")
        EnchantingChamber.bonus = "pentamid"
        player["mage_training_arena_shapes_converted"] = 9
        player.inventory.add("cylinder")
        player.inventory.add("cosmic_rune")
        player.inventory.add("water_rune")

        enchant(player, "cylinder")

        assertEquals(1, PizazzPoints.get(player, "enchanting"))
        assertEquals(0, player["mage_training_arena_shapes_converted", 0])
    }

    @Test
    fun `The bonus shape scores an extra point`() {
        val player = enter("mta-ench-bonus")
        EnchantingChamber.bonus = "icosahedron"
        player.inventory.add("icosahedron")
        player.inventory.add("cosmic_rune")
        player.inventory.add("water_rune")

        enchant(player, "icosahedron")

        assertEquals(1, PizazzPoints.get(player, "enchanting"))
        assertTrue(player.containsMessage("You get 1 bonus point!"))
    }

    @Test
    fun `Dragonstones score double the spell level immediately`() {
        val player = enter("mta-ench-dragonstone")
        player.inventory.add("dragonstone_mage_training_arena")
        player.inventory.add("cosmic_rune")
        player.inventory.add("water_rune")

        enchant(player, "dragonstone_mage_training_arena")

        assertEquals(2, PizazzPoints.get(player, "enchanting"))
        assertTrue(player.inventory.contains("orb"))
    }

    @Test
    fun `Twenty orbs in the hole reward runes`() {
        val player = enter("mta-ench-hole")
        player["mage_training_arena_orbs_deposited"] = 5
        player.inventory.add("orb", 15)
        val hole = MageTrainingArenaTest.findObject("orb_hole", Tile(3335, 9612), Tile(3389, 9663))
        player.tele(hole.tile.addY(-1))
        tick(2)

        player.objectOption(hole, "Deposit")
        tick(5)
        player.skipDialogues()

        assertEquals(0, player.inventory.count("orb"))
        assertEquals(0, player["mage_training_arena_orbs_deposited", 0])
        assertEquals(3, listOf("death_rune", "blood_rune", "cosmic_rune").sumOf { player.inventory.count(it) })
    }

    private fun enchant(player: Player, item: String) {
        player.interfaceOnItem("modern_spellbook", "enchant_level_1", Item(item), player.inventory.indexOf(item))
        tick(3)
    }

    private fun enter(name: String): Player {
        val player = createPlayer(Tile(3361, 3316), name)
        player.levels.set(Skill.Magic, 50)
        player["mage_training_arena_started"] = true
        player.inventory.add("progress_hat")
        player.tele(Tile(3363, 9649))
        tick(2)
        assertEquals("enchanting", player["mage_training_arena_room", ""])
        return player
    }
}
