package content.minigame.mage_training_arena

import WorldTest
import containsMessage
import content.entity.combat.hit.directHit
import interfaceOption
import itemOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class CreatureGraveyardTest : WorldTest() {

    @Test
    fun `Bone piles hand out bones then change type`() {
        val player = enter("mta-grave-pile")
        val pile = MageTrainingArenaTest.findObject("bones_mage_training_arena_1", Tile(3333, 9610, 1), Tile(3390, 9663, 1))
        player.tele(pile.tile.addY(-1))
        tick(2)

        repeat(4) {
            player.objectOption(GameObjects.find(pile.tile) { it.id.startsWith("bones_mage_training_arena") }, "Grab")
            tick(3)
        }

        assertEquals(4, player.inventory.count("animals_bones_1"))
        assertNotNull(GameObjects.findOrNull(pile.tile, "bones_mage_training_arena_2"))
    }

    @Test
    fun `Animal bones convert to a multiple of fruit`() {
        val player = enter("mta-grave-cast")
        player.inventory.add("animals_bones_3", 2)
        player.inventory.add("nature_rune")
        player.inventory.add("water_rune", 2)
        player.inventory.add("earth_rune", 2)

        player.interfaceOption("modern_spellbook", "bones_to_bananas", "Cast")
        tick(1)

        assertEquals(0, player.inventory.count("animals_bones_3"))
        assertEquals(6, player.inventory.count("banana"))
        assertEquals(0, player.inventory.count("nature_rune"))
        assertEquals(25.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Tablets can't be broken in the graveyard`() {
        val player = enter("mta-grave-tablet")
        player.inventory.add("animals_bones_1")
        player.inventory.add("bones_to_bananas")

        player.itemOption("Break", "bones_to_bananas")
        tick(1)

        assertTrue(player.inventory.contains("bones_to_bananas"))
        assertTrue(player.inventory.contains("animals_bones_1"))
        assertTrue(player.containsMessage("You can not use this tablet in the Mage Training Arena."))
    }

    @Test
    fun `Sixteen fruit through the chute rewards a point, a rune and experience`() {
        val player = enter("mta-grave-chute")
        player["mage_training_arena_fruit_deposited"] = 10
        player.inventory.add("banana", 4)
        player.inventory.add("peach", 4)
        val chute = MageTrainingArenaTest.findObject("food_chute", Tile(3333, 9610, 1), Tile(3390, 9663, 1))
        player.tele(chute.tile.addY(-1))
        tick(2)

        player.objectOption(chute, "Deposit")
        tick(5)
        player.skipDialogues()

        assertEquals(0, player.inventory.count("banana"))
        assertEquals(0, player.inventory.count("peach"))
        assertEquals(2, player["mage_training_arena_fruit_deposited", 0])
        assertEquals(1, PizazzPoints.get(player, "graveyard"))
        assertEquals(50.0, player.experience.get(Skill.Magic))
        assertEquals(1, listOf("nature_rune", "water_rune", "earth_rune").sumOf { player.inventory.count(it) })
    }

    @Test
    fun `Falling bones damage everyone in the room`() {
        val player = enter("mta-grave-bones")
        val before = player.levels.get(Skill.Constitution)

        tick(12)

        assertTrue(player.levels.get(Skill.Constitution) < before) { "Expected damage below $before" }
    }

    @Test
    fun `Dying in the graveyard costs up to ten points and keeps items`() {
        val player = enter("mta-grave-death")
        player["mage_training_arena_graveyard_points"] = 15
        player.inventory.add("banana", 3)

        player.directHit(player.levels.get(Skill.Constitution))
        tick(10)

        assertEquals(5, PizazzPoints.get(player, "graveyard"))
        assertTrue(player.containsMessage("You lost 10 Pizazz Points upon death!"))
        assertEquals(MageTrainingArena.lobby, player.tile)
        assertTrue(player.inventory.contains("progress_hat"))
    }

    private fun enter(name: String): Player {
        val player = createPlayer(Tile(3361, 3316), name)
        player.levels.set(Skill.Magic, 50)
        player["mage_training_arena_started"] = true
        player.inventory.add("progress_hat")
        player.tele(Tile(3363, 9639, 1))
        tick(2)
        assertEquals("graveyard", player["mage_training_arena_room", ""])
        return player
    }
}
