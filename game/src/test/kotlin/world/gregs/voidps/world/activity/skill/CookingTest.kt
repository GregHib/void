package world.gregs.voidps.world.activity.skill

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.script.*
import kotlin.test.assertFalse

internal class CookingTest : WorldTest() {

    @Test
    fun `Cooking a raw item gives a cooked item and experience`() {
        val start = emptyTile
        val player = createPlayer("chef", start)
        player.levels.set(Skill.Cooking, 100)
        player.inventory.add("raw_shrimps", 3)
        val fire = createObject("fire_orange", emptyTile.addY(1))

        player.itemOnObject(fire, 0, "")
        tick()
        player.interfaceOption("skill_creation_amount", "increment")
        player.dialogueOption("dialogue_skill_creation", "choice1")
        tick(5) // First is instant + 4 ticks for the second

        assertEquals(1, player.inventory.count("raw_shrimps"))
        assertEquals(2, player.inventory.count("shrimps"))
        assertTrue(player.experience.get(Skill.Cooking) > 0)
    }

    @Test
    fun `Mix flour and water to make bread dough`() {
        val player = createPlayer("chef", emptyTile)
        player.inventory.add("pot_of_flour")
        player.inventory.add("bowl_of_water")

        player.itemOnItem(0, 1)
        tick()
        player.dialogueOption("dialogue_skill_creation", "choice1")
        tick(2)

        assertFalse(player.inventory.contains("pot_of_flour"))
        assertFalse(player.inventory.contains("bowl_of_water"))
        assertTrue(player.inventory.contains("bread_dough"))
    }

}