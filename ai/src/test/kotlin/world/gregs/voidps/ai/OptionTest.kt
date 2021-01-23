package world.gregs.voidps.ai

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class OptionTest {

    @Test
    fun `Score combines multiple consideration values`() {
        val consideration1: Context.(Any) -> Double = { _ -> 0.5 }
        val consideration2: Context.(Any) -> Double = { _ -> 0.5 }
        val option = option(targets = { listOf("self") }, considerations = setOf(consideration1, consideration2))
        val context = context()
        // When
        val choice = option.score(context, "self")
        // Then
        assertEquals(0.5, choice, 0.12)
    }

    @Test
    fun `Score gains momentum if agent has same last goal`() {
        val consideration: Context.(Any) -> Double = { _ -> 0.4 }
        val option = option(targets = { listOf("self") }, considerations = setOf(consideration), momentum = 1.25)
        val last = Decision("self", option, 1.23)
        val context = context(last = last)
        // When
        val choice = option.score(context, "self")
        // Then
        assertEquals(0.5, choice)
    }

    @Test
    fun `Any consideration as zero returns zero`() {
        val consideration1: Context.(Any) -> Double = { _ -> 0.4 }
        val consideration2: Context.(Any) -> Double = { _ -> 0.0 }
        val option = option(targets = { listOf("self") }, considerations = setOf(consideration1, consideration2))
        val context = context()
        // When
        val choice = option.score(context, "self")
        // Then
        assertEquals(0.0, choice)
    }

    @Test
    fun `Highest scoring target selected`() {
        val consideration: Context.(Any) -> Double = { e -> if (e == "target2") 0.8 else 0.5 }
        val option = option(targets = { listOf("target1", "target2") }, considerations = setOf(consideration))
        val context = context()
        // When
        val choice = option.getHighestTarget(context, 0.0)
        // Then
        assertNotNull(choice)
        assertEquals(option, choice!!.option)
        assertEquals("target2", choice.target)
        assertEquals(0.8, choice.score)
    }

    @Test
    fun `Target ignored if score isn't higher`() {
        val consideration: Context.(Any) -> Double = { _ -> 0.5 }
        val option = option(targets = { listOf("self") }, considerations = setOf(consideration))
        val context = context()
        // When
        val choice = option.getHighestTarget(context, 0.6)
        // Then
        assertNull(choice)
    }

    @Test
    fun `Goal ignored if highest score is great than weight`() {
        val consideration: Context.(Any) -> Double = { _ -> 0.5 }
        val option = option(targets = { listOf("target") }, considerations = setOf(consideration), weight = 0.7)
        val context = context()
        // When
        val choice = option.getHighestTarget(context, 0.8)
        // Then
        assertNull(choice)
    }

    private fun context(last: Decision? = null): Context = object : Context {
        override var last: Decision? = last
    }

    private fun option(
        targets: Context.() -> List<Any>,
        considerations: Set<Context.(Any) -> Double>,
        weight: Double = 1.0,
        momentum: Double = 1.25
    ) = object : Option<Context, Any> {
        override val targets = targets
        override val considerations = considerations
        override val momentum = momentum
        override val weight = weight
    }
}