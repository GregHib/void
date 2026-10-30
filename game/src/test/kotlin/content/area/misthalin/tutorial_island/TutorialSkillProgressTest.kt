package content.area.misthalin.tutorial_island

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class TutorialSkillProgressTest : WorldTest() {
    @ParameterizedTest
    @CsvSource("Firemaking,7", "Attack,47", "Strength,47", "Defence,47", "Magic,66")
    fun `Skill refreshes do not progress the tutorial but real XP gains do`(skill: Skill, stage: Int) {
        val player = createPlayer {
            it.set("tutorial_stage", stage)
            it.set("tutorial_designed", true)
        }
        player.experience.update(skill)
        assertEquals(stage, player.tutorialStage)
        player.experience.set(skill, player.experience.direct(skill))
        assertEquals(stage, player.tutorialStage)
        player.experience.add(skill, 1.0)
        assertEquals(stage + 1, player.tutorialStage)
        player.experience.update(skill)
        assertEquals(stage + 1, player.tutorialStage)
    }
}
