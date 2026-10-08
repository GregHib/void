package content.minigame.fist_of_guthix

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.config.VariableDefinition.Companion.persist
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import kotlin.test.assertTrue

class FiaraDialoguePersistenceTest : WorldTest() {

    @Test
    fun `Fiara and druids remember prior dialogue across logout`() {
        for (key in listOf(
            "fist_of_guthix_tutorial_complete",
            "fist_of_guthix_fiara_identity",
            "fist_of_guthix_seen_fiara_introduction",
            "fist_of_guthix_completed_fiara_what_is_this_place_dialogue",
            "fist_of_guthix_spoken_to_alran",
            "fist_of_guthix_spoken_to_getorix",
            "fist_of_guthix_spoken_to_pontimer",
        )) {
            assertTrue(VariableDefinitions.get(key).persist, key)
        }
    }
}
