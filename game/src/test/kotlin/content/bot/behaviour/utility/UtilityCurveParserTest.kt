package content.bot.behaviour.utility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UtilityCurveParserTest {

    @Test
    fun `Parses linear with min and max`() {
        val curve = UtilityCurveParser.parseCurve(mapOf("type" to "linear", "min" to 0, "max" to 10))
        assertEquals(UtilityCurve.Linear(0.0, 10.0), curve)
    }

    @Test
    fun `Parses logistic with midpoint and steepness`() {
        val curve = UtilityCurveParser.parseCurve(mapOf("type" to "logistic", "midpoint" to 0.4, "steepness" to 8))
        assertEquals(UtilityCurve.Logistic(0.4, 8.0), curve)
    }

    @Test
    fun `Sine and cosine require no params`() {
        assertEquals(UtilityCurve.Sine, UtilityCurveParser.parseCurve(mapOf("type" to "sine")))
        assertEquals(UtilityCurve.Cosine, UtilityCurveParser.parseCurve(mapOf("type" to "cosine")))
    }

    @Test
    fun `Unknown type errors with clear message`() {
        val ex = assertThrows(IllegalStateException::class.java) {
            UtilityCurveParser.parseCurve(mapOf("type" to "wobble"))
        }
        assertTrue(ex.message!!.contains("wobble"))
    }

    @Test
    fun `Exponential without base errors`() {
        val ex = assertThrows(IllegalStateException::class.java) {
            UtilityCurveParser.parseCurve(mapOf("type" to "exponential"))
        }
        assertTrue(ex.message!!.contains("base"))
    }

    @Test
    fun `Parses scorer with multiple components`() {
        val scorer = UtilityCurveParser.parseScorer(
            listOf(
                mapOf(
                    "input" to "target_hp_percent",
                    "curve" to mapOf("type" to "logistic", "midpoint" to 0.4, "steepness" to 8),
                    "weight" to 3,
                ),
                mapOf(
                    "input" to "distance",
                    "curve" to mapOf("type" to "exponential", "base" to 0.9),
                    "weight" to 2,
                ),
            ),
        )
        assertEquals(2, scorer.components.size)
        assertEquals(TargetInput.TargetHpPercent, scorer.components[0].input)
        assertEquals(3.0, scorer.components[0].weight)
        assertEquals(TargetInput.Distance, scorer.components[1].input)
    }

    @Test
    fun `Unknown input key errors`() {
        val ex = assertThrows(IllegalStateException::class.java) {
            UtilityCurveParser.parseScorer(
                listOf(
                    mapOf(
                        "input" to "imaginary",
                        "curve" to mapOf("type" to "linear"),
                    ),
                ),
            )
        }
        assertTrue(ex.message!!.contains("imaginary"))
    }
}
