package content.skill.herblore

import WorldTest
import itemOption
import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals

class HerbCleaningTest : WorldTest() {

    private val herbs = listOf(
        "grimy_guam",
        "grimy_marrentill",
        "grimy_tarromin",
        "grimy_harralander",
        "grimy_ranarr",
        "grimy_irit",
        "grimy_avantoe",
        "grimy_kwuarm",
        "grimy_cadantine",
        "grimy_dwarf_weed",
        "grimy_torstol",
        "grimy_lantadyme",
        "grimy_toadflax",
        "grimy_snapdragon",
        "grimy_wergali",
        "grimy_spirit_weed",
    )

    @TestFactory
    fun `Clean grimy herb`() = herbs.map { herb ->
        dynamicTest("Clean ${herb.toSentenceCase()}") {
            val player = createPlayer()
            player.levels.set(Skill.Herblore, 99)
            player.inventory.add(herb)

            player.itemOption("Clean", herb)
            tick(2)

            assertEquals(1, player.inventory.count(herb.replace("grimy_", "clean_")))
            assertEquals(0, player.inventory.count(herb))
            assertNotEquals(0.0, player.experience.get(Skill.Herblore))
        }
    }
}
