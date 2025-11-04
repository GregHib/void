package content.skill.constitution.drink

import FakeRandom
import WorldTest
import content.entity.effect.toxin.poison
import content.entity.player.effect.energy.runEnergy
import itemOnItem
import itemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom

internal class TeaTest : WorldTest() {

    @Test
    fun `Cup of tea boosts attack`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("cup_of_tea")
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.levels.set(Skill.Constitution, 100)

        player.itemOption("Drink", "cup_of_tea")

        assertTrue(player.inventory.contains("empty_cup"))
        assertEquals(130, player.levels.get(Skill.Constitution))
        assertEquals(4, player.levels.get(Skill.Attack))
    }

    @Test
    fun `Guthix rest cures poison and boosts energy and health points`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int): Int = 25
        })
        val player = createPlayer(emptyTile)
        player.inventory.add("guthix_rest_3")
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.poison(player, 50)
        player.runEnergy = 0
        player.levels.set(Skill.Constitution, 100)

        player.itemOption("Drink", "guthix_rest_3")

        assertTrue(player.inventory.contains("guthix_rest_2"))
        assertEquals(150, player.levels.get(Skill.Constitution))
        assertEquals(40, player["poison_damage", 0])
        assertEquals(500, player.runEnergy)
    }

    @Test
    fun `Nettle tea boosts energy`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("nettle_tea")
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.levels.set(Skill.Constitution, 100)
        player.runEnergy = 0

        player.itemOption("Drink", "nettle_tea")

        assertTrue(player.inventory.contains("bowl"))
        assertEquals(130, player.levels.get(Skill.Constitution))
        assertEquals(500, player.runEnergy)
    }

    @Test
    fun `Tea flask boosts attack`() {
        val player = createPlayer(emptyTile)
        player.inventory.set(0, "tea_flask", 5)
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.levels.set(Skill.Constitution, 100)

        player.itemOption("Drink", "tea_flask")

        assertTrue(player.inventory.contains("tea_flask"))
        assertEquals(130, player.levels.get(Skill.Constitution))
        assertEquals(4, player.levels.get(Skill.Attack))
        assertEquals(4, player.inventory.charges(player, 0))
    }

    @Test
    fun `Can't drink from empty tea flask`() {
        val player = createPlayer(emptyTile)
        player.inventory.set(0, "tea_flask", 0)
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.levels.set(Skill.Constitution, 100)

        player.itemOption("Drink", "tea_flask")

        assertTrue(player.inventory.contains("tea_flask"))
        assertEquals(100, player.levels.get(Skill.Constitution))
        assertEquals(1, player.levels.get(Skill.Attack))
        assertEquals(0, player.inventory.charges(player, 0))
    }

    @Test
    fun `Pour tea with flask`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("tea_flask")
        player.inventory.add("empty_cup")

        player.itemOnItem(0, 1)

        assertTrue(player.inventory.contains("cup_of_tea"))
        assertEquals(0, player.inventory.charges(player, 0))
    }

    @Test
    fun `Pour tea into flask`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("tea_flask")
        player.inventory.add("cup_of_tea")

        player.itemOnItem(1, 0)

        assertTrue(player.inventory.contains("empty_cup"))
        assertEquals(2, player.inventory.charges(player, 0))
    }

    @Test
    fun `Can't fill full tea flask`() {
        val player = createPlayer(emptyTile)
        player.inventory.set(0, "tea_flask", 5)
        player.inventory.add("cup_of_tea")

        player.itemOnItem(1, 0)

        assertTrue(player.inventory.contains("cup_of_tea"))
        assertEquals(5, player.inventory.charges(player, 0))
    }
}
