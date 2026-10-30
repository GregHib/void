package world.gregs.voidps.tools.cache

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition

internal class RemoveRegistrationAgeLimitTest {

    /**
     * ```
     * 0: push 1
     * 1: if equal jump to 3
     * 2: jump to 19
     * 3: switch 5 -> 19
     * 4..16: age check
     * 17: send create account packet
     * 18: jump to 20
     * 19: push "msg"
     * 20: return
     * ```
     */
    private fun script(): ClientScriptDefinition {
        val instructions = listOf(0 to 1, 8 to 1, 6 to 16, 51 to 0) + RemoveRegistrationAgeLimit.AGE_CHECK + listOf(5605 to 0, 6 to 1, 3 to 0, 21 to 0)
        return ClientScriptDefinition(
            id = RemoveRegistrationAgeLimit.SCRIPT_ID,
            instructions = instructions.map { it.first }.toIntArray(),
            intOperands = instructions.map { it.second }.toIntArray(),
            stringOperands = arrayOfNulls<String>(instructions.size).apply { this[19] = "msg" },
            switchStatementIndices = arrayOf(listOf(5 to 15)),
        )
    }

    @Test
    fun `Remove age check and re-target jumps over it`() {
        val script = script()

        assertTrue(RemoveRegistrationAgeLimit.remove(script))

        assertArrayEquals(intArrayOf(0, 8, 6, 51, 5605, 6, 3, 21), script.instructions)
        assertArrayEquals(intArrayOf(1, 1, 3, 0, 0, 1, 0, 0), script.intOperands)
        assertEquals("msg", script.stringOperands!![6])
        assertEquals(listOf(5 to 2), script.switchStatementIndices!![0])
    }

    @Test
    fun `Jump to the first removed instruction continues after it`() {
        val script = script()
        script.intOperands!![2] = 1 // jump to 4

        assertTrue(RemoveRegistrationAgeLimit.remove(script))

        assertArrayEquals(intArrayOf(0, 8, 6, 51, 5605, 6, 3, 21), script.instructions)
        assertEquals(1, script.intOperands!![2])
    }

    @Test
    fun `Already removed`() {
        val script = script()
        RemoveRegistrationAgeLimit.remove(script)

        assertFalse(RemoveRegistrationAgeLimit.remove(script))
    }

    @Test
    fun `Jump into removed instructions throws`() {
        val script = script()
        script.intOperands!![2] = 5 // jump to 8

        assertThrows<IllegalStateException> {
            RemoveRegistrationAgeLimit.remove(script)
        }
    }
}
