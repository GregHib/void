package content.skill.farming

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import kotlin.test.assertTrue

class AllotmentInspectionTest : WorldTest() {
    @Test
    fun `Inspect every allotment crop growth state`() {
        val crops = mapOf(
            "potato" to 3,
            "onion" to 3,
            "cabbage" to 3,
            "tomato" to 3,
            "sweetcorn" to 5,
            "strawberry" to 5,
            "watermelon" to 7,
        )
        val variable = "farming_veg_patch_falador_se"
        val patch = GameObjects.find(Tile(3058, 3308), variable)

        for ((crop, maximumStage) in crops) {
            for (stage in 0..maximumStage) {
                for (watered in listOf(false, true)) {
                    val player = createPlayer(Tile(3057, 3308))
                    val state = if (watered) "${crop}_watered_$stage" else "${crop}_$stage"
                    player[variable] = state

                    player.objectOption(patch, "Inspect")
                    tick()

                    val expected = "at state ${stage + 1}/${maximumStage + 2}"
                    assertTrue(
                        player.containsMessage(expected),
                        "$state did not report '$expected'",
                    )
                    assertTrue(
                        !patch.def(player).stringId.endsWith("_fullygrown"),
                        "$state resolved to ${patch.def(player).stringId}",
                    )
                }
            }

            val fullyGrownPlayer = createPlayer(Tile(3057, 3308))
            fullyGrownPlayer[variable] = "${crop}_life1"
            fullyGrownPlayer.objectOption(patch, "Inspect")
            tick()

            assertTrue(fullyGrownPlayer.containsMessage("fully grown"))
            assertTrue(patch.def(fullyGrownPlayer).stringId.endsWith("_fullygrown"))
        }
    }
}
