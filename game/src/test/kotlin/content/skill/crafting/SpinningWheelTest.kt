package content.skill.crafting

import WorldTest
import containsMessage
import continueDialogue
import intEntry
import interfaceOption
import itemOnObject
import net.pearx.kasechange.toLowerSpaceCase
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class SpinningWheelTest : WorldTest() {

    @Test
    fun `Spinning wheel`() {
        val player = createPlayer(Tile(3082, 3430))
        player.inventory.add("wool")

        val wheel = GameObjects.find(Tile(3081, 3430), "spinning_wheel_barbarian_village")
        player.itemOnObject(wheel, 0)
        tick(1)
        player.continueDialogue("dialogue_skill_creation", "choice1")
        tick(3)

        assertEquals(0, player.inventory.count("wool"))
        assertEquals(1, player.inventory.count("ball_of_wool"))
        assertNotEquals(0.0, player.experience.get(Skill.Crafting))
    }

}
