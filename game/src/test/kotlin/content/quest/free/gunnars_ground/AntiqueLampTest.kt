package content.quest.free.gunnars_ground

import WorldTest
import interfaceOption
import itemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class AntiqueLampTest : WorldTest() {

    @Test
    fun `rub antique lamp with skill over level 5`() {
        val player = createPlayer()
        val lampId = "antique_lamp_gunnars_ground"

        player.inventory.add(lampId)
        player.levels.set(Skill.Attack, 5)
        player.levels.set(Skill.Defence, 4)
        player.levels.set(Skill.Magic, 6)
        val initialExperience = player.experience.get(Skill.Magic)

        player.itemOption("Rub", lampId)
        player.interfaceOption(
            "skill_stat_advance",
            "magic",
            "Select",
            optionIndex = 0,
        )
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")

        assertEquals(0, player.inventory.count(lampId))
        assertEquals(initialExperience + 200.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `rub antique lamp with skill at level 5`() {
        val player = createPlayer()
        val lampId = "antique_lamp_gunnars_ground"

        player.inventory.add(lampId)
        player.levels.set(Skill.Attack, 5)
        player.levels.set(Skill.Defence, 4)
        player.levels.set(Skill.Magic, 6)
        val initialExperience = player.experience.get(Skill.Attack)

        player.itemOption("Rub", lampId)
        player.interfaceOption(
            "skill_stat_advance",
            "attack",
            "Select",
            optionIndex = 0,
        )
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")

        assertEquals(0, player.inventory.count(lampId))
        assertEquals(initialExperience + 200.0, player.experience.get(Skill.Attack))
    }

    @Test
    fun `rub antique lamp with skill below level 5`() {
        val player = createPlayer()
        val lampId = "antique_lamp_gunnars_ground"

        player.inventory.add(lampId)
        player.levels.set(Skill.Attack, 5)
        player.levels.set(Skill.Defence, 4)
        player.levels.set(Skill.Magic, 6)
        val initialExperience = player.experience.get(Skill.Defence)

        player.itemOption("Rub", lampId)
        player.interfaceOption(
            "skill_stat_advance",
            "defence",
            "Select",
            optionIndex = 0,
        )
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")

        assertEquals(1, player.inventory.count(lampId))
        assertEquals(initialExperience, player.experience.get(Skill.Defence))
    }
}
