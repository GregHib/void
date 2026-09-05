package content.skill.constitution.drink

import FakeRandom
import WorldTest
import itemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom

internal class PoisonChaliceTest : WorldTest() {

    @Test
    fun `Poison chalice can invigorate the drinker`() {
        setRandom(
            object : FakeRandom() {
                override fun nextInt(from: Int, until: Int): Int = if (until == 7) 5 else from
            },
        )
        val player = createPlayer(emptyTile)
        player.inventory.add("poison_chalice")

        player.itemOption("Drink", "poison_chalice")

        assertTrue(player.inventory.contains("cocktail_glass"))
        assertEquals(5, player.levels.get(Skill.Attack))
        assertEquals(5, player.levels.get(Skill.Strength))
        assertEquals(5, player.levels.get(Skill.Defence))
        assertEquals(2, player.levels.get(Skill.Thieving))
    }

    @Test
    fun `Poison chalice can make the drinker ill`() {
        setRandom(
            object : FakeRandom() {
                override fun nextInt(from: Int, until: Int): Int = when (until) {
                    7 -> 6
                    50 -> 30
                    else -> from
                }
            },
        )
        val player = createPlayer(emptyTile)
        player.inventory.add("poison_chalice")
        player.experience.set(Skill.Constitution, Level.experience(10))

        player.itemOption("Drink", "poison_chalice")

        assertTrue(player.inventory.contains("cocktail_glass"))
        assertEquals(2, player.levels.get(Skill.Attack))
        assertEquals(70, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Poison chalice can drain the drinker`() {
        setRandom(
            object : FakeRandom() {
                override fun nextInt(from: Int, until: Int): Int = if (until == 7) 2 else from
            },
        )
        val player = createPlayer(emptyTile)
        player.inventory.add("poison_chalice")
        player.experience.set(Skill.Attack, Level.experience(10))
        player.levels.set(Skill.Attack, 10)

        player.itemOption("Drink", "poison_chalice")

        assertEquals(9, player.levels.get(Skill.Attack))
        assertEquals(2, player.levels.get(Skill.Thieving))
    }

    @Test
    fun `Cadava potion cannot be drunk`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("cadava_potion")

        player.itemOption("Drink", "cadava_potion")

        assertTrue(player.interfaces.contains("dialogue_obj_box"))
        assertTrue(player.inventory.contains("cadava_potion"))
    }
}
