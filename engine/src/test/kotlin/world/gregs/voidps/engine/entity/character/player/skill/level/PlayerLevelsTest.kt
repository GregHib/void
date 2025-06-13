package world.gregs.voidps.engine.entity.character.player.skill.level

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience

internal class PlayerLevelsTest {

    @Test
    fun `Get level for decimal value under`() {
        assertEquals(9, Experience.level(Skill.Attack, 1153.9))
    }

    @Test
    fun `Get level for exact decimal value`() {
        assertEquals(10, Experience.level(Skill.Attack, 1154.0))
    }

    @Test
    fun `Get level for decimal value over`() {
        assertEquals(10, Experience.level(Skill.Attack, 1154.1))
    }
}
