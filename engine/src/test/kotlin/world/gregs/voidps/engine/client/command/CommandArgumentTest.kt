package world.gregs.voidps.engine.client.command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CommandArgumentTest {

    @Test
    fun `Can parse integer`() {
        val arg = CommandArgument("test", ArgType.Int)
        assertTrue(arg.canParse("123"))
    }

    @Test
    fun `Can parse SI suffixed integers`() {
        val arg = CommandArgument("test", ArgType.Int)
        assertTrue(arg.canParse("123k"))
        assertTrue(arg.canParse("123m"))
        assertTrue(arg.canParse("123b"))
        assertTrue(arg.canParse("123t"))
        assertFalse(arg.canParse("123q"))
    }

    @Test
    fun `Int can't parse double`() {
        val arg = CommandArgument("test", ArgType.Int)
        assertFalse(arg.canParse("123.0"))
    }

    @Test
    fun `Can parse double`() {
        val arg = CommandArgument("test", ArgType.Double)
        assertTrue(arg.canParse("123.0"))
    }

    @Test
    fun `Can parse boolean`() {
        val arg = CommandArgument("test", ArgType.Boolean)
        assertTrue(arg.canParse("true"))
        assertTrue(arg.canParse("false"))
        assertFalse(arg.canParse("1"))
        assertFalse(arg.canParse("yes"))
    }

}