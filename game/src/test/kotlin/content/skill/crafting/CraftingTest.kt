package content.skill.crafting

import WorldTest
import containsMessage
import continueDialogue
import intEntry
import interfaceOption
import itemOnItem
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

class CraftingTest : WorldTest() {

    @Test
    fun `Craft with thread and needle`() {
        val player = createPlayer(Tile(3082, 3430))
        player.inventory.add("needle")
        player.inventory.add("thread")
        player.inventory.add("leather")

        player.itemOnItem(0, 2)
        tick(1)
        player.continueDialogue("dialogue_skill_creation", "choice1")
        tick(3)

        assertEquals(1, player.inventory.count("needle"))
        assertEquals(1, player.inventory.count("thread"))
        assertEquals(0, player.inventory.count("leather"))
        assertEquals(1, player.inventory.count("leather_gloves"))
        assertNotEquals(0.0, player.experience.get(Skill.Crafting))
    }

    @Test
    fun `Use thread when crafting multiple`() {
        val player = createPlayer(Tile(3082, 3430))
        player.levels.set(Skill.Crafting, 99)
        player.inventory.add("needle")
        player.inventory.add("thread")
        player.inventory.add("red_dragon_leather")
        player["thread_used"] = 4

        player.itemOnItem(0, 2)
        tick(1)
        player.continueDialogue("dialogue_skill_creation", "choice1")
        tick(3)

        assertEquals(1, player.inventory.count("needle"))
        assertEquals(0, player.inventory.count("thread"))
        assertEquals(0, player.inventory.count("red_dragon_leather"))
        assertEquals(1, player.inventory.count("red_dragonhide_vambraces"))
        assertNotEquals(0.0, player.experience.get(Skill.Crafting))
    }

}
