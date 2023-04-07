package world.gregs.voidps.engine.entity.character.player.skill.level

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill

internal class PlayerLevelsTest {

    @Test
    fun `Get level for decimal value under`() {
        assertEquals(9, PlayerLevels.getLevel(1153.9, Skill.Attack))
    }

    @Test
    fun `Get level for exact decimal value`() {
        assertEquals(10, PlayerLevels.getLevel(1154.0, Skill.Attack))
    }

    @Test
    fun `Get level for decimal value over`() {
        assertEquals(10, PlayerLevels.getLevel(1154.1, Skill.Attack))
    }
}