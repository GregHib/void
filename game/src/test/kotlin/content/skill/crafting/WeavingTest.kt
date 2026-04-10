package content.skill.crafting

import WorldTest
import continueDialogue
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class WeavingTest : WorldTest() {

    @Test
    fun `Weave strip of cloth`() {
        val player = createPlayer(Tile(3039, 3286))
        player.levels.set(Skill.Crafting, 10)
        player.inventory.add("ball_of_wool", 4)

        val loom = GameObjects.find(Tile(3039, 3287), "loom_falador_farm")

        player.objectOption(loom, "Weave")
        tick(1)
        player.continueDialogue("dialogue_skill_creation", "choice1")
        tick(4)

        assertEquals(0, player.inventory.count("ball_of_wool"))
        assertEquals(1, player.inventory.count("strip_of_cloth"))
        assertNotEquals(0.0, player.experience.get(Skill.Crafting))
    }
}
