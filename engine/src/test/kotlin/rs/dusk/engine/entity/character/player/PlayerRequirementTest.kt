package rs.dusk.engine.entity.character.player

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PlayerRequirementTest {

    @Test
    fun `Player lacks requirement`() {
        val player: Player = mockk()
        var called = false
        val requirement = object : PlayerRequirement {
            override fun onFailure(player: Player) {
                called = true
            }

            override fun met(player: Player): Boolean {
                return false
            }

        }
        assertTrue(player.lacks(requirement))
        assertTrue(called)
    }

    @Test
    fun `Player meets requirement`() {
        val player: Player = mockk()
        var called = false
        val requirement = object : PlayerRequirement {
            override fun onFailure(player: Player) {
                called = true
            }

            override fun met(player: Player): Boolean {
                return true
            }

        }
        assertFalse(player.lacks(requirement))
        assertFalse(called)
    }
}