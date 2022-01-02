package world.gregs.voidps.world.activity.skill

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.dialogueOption
import world.gregs.voidps.world.script.itemOnItem
import world.gregs.voidps.world.script.itemOnObject
import kotlin.test.assertFalse

internal class CookingTest : WorldMock() {

    @Test
    fun `Cooking a raw item gives a cooked item and experience`() = runBlocking(Dispatchers.Default) {
        val start = Tile(100, 100)
        val player = createPlayer("chef", start)
        player.levels.setOffset(Skill.Cooking, 100)
        player.inventory.add("raw_shrimps")
        val fire = createObject("fire_orange", Tile(100, 101))

        player.itemOnObject(fire, 0, "")
        tick()
        player.dialogueOption("dialogue_skill_creation", "choice1")
        tick(4)

        assertFalse(player.inventory.contains("raw_shrimps"))
        assertTrue(player.inventory.contains("shrimps"))
        assertTrue(player.experience.get(Skill.Cooking) > 0)
    }

    @Test
    fun `Mix flour and water to make bread dough`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("chef", Tile(100, 100))
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