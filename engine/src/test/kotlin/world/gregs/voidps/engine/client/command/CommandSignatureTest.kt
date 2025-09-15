package world.gregs.voidps.engine.client.command

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CommandSignatureTest {

    @Test
    fun `No score when input size smaller than required`() {
        val sig = sig(stringArg("a"), stringArg("b")) // 2 required
        assertNull(sig.score(listOf("only-one")))
    }

    @Test
    fun `No score when input size is larger than max args`() {
        val sig = sig(stringArg("a"))
        assertNull(sig.score(listOf("one", "two")))
    }

    @Test
    fun `No score when argument can't be parsed`() {
        val sig = sig(intArg("num"))
        assertNull(sig.score(listOf("not-a-number")))
    }

    @Test
    fun `Score equals number of successfully parsed args`() {
        val sig = sig(stringArg("a"), stringArg("b"))
        Assertions.assertEquals(2, sig.score(listOf("thing1", "thing2")))
    }

    @Test
    fun `Optional arguments are optional`() {
        val sig = sig(stringArg("a"), stringArg("b", optional = true),)

        Assertions.assertEquals(1, sig.score(listOf("required")))
        Assertions.assertEquals(2, sig.score(listOf("required", "optional")))
    }

    @Test
    fun `Empty args and input scores zero`() {
        val sig = sig()
        Assertions.assertEquals(0, sig.score(emptyList()))
    }

    private fun sig(vararg args: CommandArgument) = CommandSignature(args.toList()) { player, args -> }

}