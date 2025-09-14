package world.gregs.voidps.engine.client.command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CommandTest {

    private fun command(vararg signatures: CommandSignature) = Command("test", signatures = signatures.toList())

    private fun sig(vararg args: CommandArgument) =
        CommandSignature(args.toList()) { _, _ -> }

    @Test
    fun `No signatures`() {
        val meta = command()
        assertNull(meta.find(listOf("anything")))
    }

    @Test
    fun `No signature matches`() {
        val meta = command(sig(intArg("num")))
        assertNull(meta.find(listOf("not-a-number")))
    }

    @Test
    fun `Matching signature`() {
        val s1 = sig(stringArg("a"))
        val meta = command(s1)
        val result = meta.find(listOf("foo"))
        assertEquals(s1, result)
    }

    @Test
    fun `Highest score when multiple match`() {
        val s1 = sig(stringArg("a"))
        val s2 = sig(stringArg("a"), stringArg("b"))
        val meta = command(s1, s2)
        val result = meta.find(listOf("foo", "bar"))
        assertEquals(s2, result)
    }

    @Test
    fun `First signature if scores are equal`() {
        val s1 = sig(stringArg("a"))
        val s2 = sig(stringArg("b"))
        val meta = command(s1, s2)
        val result = meta.find(listOf("x"))
        assertEquals(s1, result)
    }

}