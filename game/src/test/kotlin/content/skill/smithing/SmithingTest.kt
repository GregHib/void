package content.skill.smithing

import WorldTest
import interfaceOption
import itemOnObject
import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class SmithingTest : WorldTest() {

    private val bars = listOf(
        "bronze_bar",
        "iron_bar",
        "steel_bar",
        "mithril_bar",
        "adamant_bar",
        "rune_bar",
    )

    @TestFactory
    fun `Smith an item on an anvil`() = bars.map { bar ->
        dynamicTest("Smith ${bar.toSentenceCase()}") {
            val player = createPlayer(Tile(3187, 3426))
            player.levels.set(Skill.Smithing, 99)
            player.inventory.add("hammer")
            player.inventory.add(bar)
            val furnace = GameObjects.find(Tile(3188, 3426), "anvil")

            player.itemOnObject(furnace, 1)
            tick()

            player.interfaceOption("smithing", "dagger_1", "Make 1 Dagger", optionIndex = 0)
            tick(5)

            assertEquals(1, player.inventory.count(bar.replace("_bar", "_dagger")))
            assertNotEquals(0.0, player.experience.get(Skill.Smithing))
        }
    }
}
