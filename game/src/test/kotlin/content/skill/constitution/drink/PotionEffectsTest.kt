package content.skill.constitution.drink

import WorldTest
import content.entity.player.effect.energy.runEnergy
import itemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

internal class PotionEffectsTest : WorldTest() {

    @Test
    fun `Energy potion adds a tenth of maximum run energy`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("energy_potion_4")
        player.runEnergy = 5000

        player.itemOption("Drink", "energy_potion_4")

        assertTrue(player.inventory.contains("energy_potion_3"))
        assertEquals(6000, player.runEnergy)
    }

    @Test
    fun `Super energy potion adds a fifth of maximum run energy`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("super_energy_4")
        player.runEnergy = 5000

        player.itemOption("Drink", "super_energy_4")

        assertTrue(player.inventory.contains("super_energy_3"))
        assertEquals(7000, player.runEnergy)
    }

    @Test
    fun `Two dose strength potion can be drunk`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("strength_potion_2")

        player.itemOption("Drink", "strength_potion_2")

        assertTrue(player.inventory.contains("strength_potion_1"))
        assertEquals(4, player.levels.get(Skill.Strength))
    }

    @Test
    fun `Weak ranged potion boosts ranged`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("weak_ranged_potion")

        player.itemOption("Drink", "weak_ranged_potion")

        assertTrue(player.inventory.contains("vial_dungeoneering"))
        assertEquals(3, player.levels.get(Skill.Ranged))
    }

    @Test
    fun `Antidote plus mix keeps its own dose chain`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("antidote+_mix_2")
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.levels.set(Skill.Constitution, 100)

        player.itemOption("Drink", "antidote+_mix_2")

        assertTrue(player.inventory.contains("antidote+_mix_1"))
    }

    @Test
    fun `Each antipoison tier gives its own immunity duration`() {
        val tiers = mapOf(
            "antipoison_4" to TimeUnit.SECONDS.toTicks(90),
            "super_antipoison_4" to TimeUnit.MINUTES.toTicks(6),
            "antipoison+_4" to TimeUnit.MINUTES.toTicks(9),
            "antipoison++_4" to TimeUnit.MINUTES.toTicks(12),
        )
        for ((potion, ticks) in tiers) {
            val player = createPlayer(emptyTile)
            player.inventory.add(potion)

            player.itemOption("Drink", potion)

            assertEquals(-(ticks * 2), player["poison", 0], potion)
        }
    }

    @Test
    fun `Antidote plus mix gives nine minutes of immunity`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("antidote+_mix_2")
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.levels.set(Skill.Constitution, 100)

        player.itemOption("Drink", "antidote+_mix_2")

        assertEquals(-(TimeUnit.MINUTES.toTicks(9) * 2), player["poison", 0])
    }

    @Test
    fun `Food ending in a dose suffix is not treated as a potion`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("cooked_crab_meat_4")
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.levels.set(Skill.Constitution, 100)

        player.itemOption("Eat", "cooked_crab_meat_4")

        assertTrue(player.inventory.contains("cooked_crab_meat_3"))
    }

    @Test
    fun `Extreme attack potion boosts attack past a super attack potion`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("extreme_attack_4")
        player.experience.set(Skill.Attack, Level.experience(99))
        player.levels.set(Skill.Attack, 99)

        player.itemOption("Drink", "extreme_attack_4")

        assertTrue(player.inventory.contains("extreme_attack_3"))
        assertEquals(99 + 5 + 21, player.levels.get(Skill.Attack))
    }

    @Test
    fun `Overload boosts ranged by the same amount as an extreme ranging potion`() {
        val drinker = createPlayer(emptyTile)
        drinker.inventory.add("extreme_ranging_4")
        drinker.experience.set(Skill.Ranged, Level.experience(99))
        drinker.levels.set(Skill.Ranged, 99)

        drinker.itemOption("Drink", "extreme_ranging_4")

        val overloaded = createPlayer(emptyTile)
        overloaded.inventory.add("overload_4")
        overloaded.experience.set(Skill.Constitution, Level.experience(99))
        overloaded.levels.set(Skill.Constitution, 990)
        overloaded.experience.set(Skill.Ranged, Level.experience(99))
        overloaded.levels.set(Skill.Ranged, 99)

        overloaded.itemOption("Drink", "overload_4")

        assertEquals(drinker.levels.get(Skill.Ranged), overloaded.levels.get(Skill.Ranged))
    }

    @Test
    fun `Super prayer potion restores more than a prayer potion`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("super_prayer_4")
        player.experience.set(Skill.Prayer, Level.experience(99))
        player.levels.set(Skill.Prayer, 1)

        player.itemOption("Drink", "super_prayer_4")

        assertTrue(player.inventory.contains("super_prayer_3"))
        assertEquals(1 + 7 + 34, player.levels.get(Skill.Prayer))
    }
}
