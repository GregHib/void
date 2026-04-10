package content.skill.crafting

import WorldTest
import continueDialogue
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class PotteryTest : WorldTest() {

    @Test
    fun `Pottery wheel`() {
        val player = createPlayer(Tile(3086, 3410))
        player.inventory.add("soft_clay")

        val wheel = GameObjects.find(Tile(3087, 3410), "potters_wheel")
        player.itemOnObject(wheel, 0)
        tick(1)
        player.continueDialogue("dialogue_skill_creation", "choice1")
        tick(3)

        assertEquals(0, player.inventory.count("soft_clay"))
        assertEquals(1, player.inventory.count("unfired_pot"))
        assertNotEquals(0.0, player.experience.get(Skill.Crafting))
    }

    @Test
    fun `Pottery oven firing`() {
        val player = createPlayer(Tile(3085, 3408))
        player.inventory.add("unfired_pot")

        val oven = GameObjects.find(Tile(3084, 3407), "pottery_oven")
        player.objectOption(oven, "Fire")
        tick(1)
        player.continueDialogue("dialogue_skill_creation", "choice1")
        tick(3)
        assertEquals(0, player.inventory.count("unfired_pot"))
        assertEquals(1, player.inventory.count("empty_pot"))
        assertNotEquals(0.0, player.experience.get(Skill.Crafting))
    }
}
