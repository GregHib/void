package content.skill.woodcutting

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import WorldTest
import objectOption

internal class WoodcuttingTest : WorldTest() {

    @Test
    fun `Woodcutting gives log and depletes`() {
        val player = createPlayer("jack", emptyTile)
        player.levels.set(Skill.Woodcutting, 100)
        val tile = emptyTile.addY(1)
        val tree = createObject("tree", tile)
        player.inventory.add("bronze_hatchet")

        player.objectOption(tree, "Chop down")
        tickIf { player.inventory.spaces >= 27 }

        assertTrue(player.inventory.contains("logs"))
        assertTrue(player.experience.get(Skill.Woodcutting) > 0)
        assertNotEquals(tree.id, objects.getLayer(tile, ObjectLayer.GROUND)?.id)
    }

}