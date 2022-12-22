package world.gregs.voidps.world.activity.skill

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.objectOption

internal class WoodcuttingTest : WorldTest() {

    lateinit var objects: Objects

    @BeforeEach
    fun start() {
        objects = get()
    }

    @Test
    fun `Woodcutting gives log and depletes`() {
        World.events.set(emptyMap())
        val player = createPlayer("jack", emptyTile)
        player.levels.set(Skill.Woodcutting, 100)
        val tile = emptyTile.addY(1)
        val tree = createObject("tree", tile)
        player.inventory.add("bronze_hatchet")

        player.objectOption(tree, "Chop down")
        tickIf { player.inventory.spaces >= 27 }

        assertTrue(player.inventory.contains("logs"))
        assertTrue(player.experience.get(Skill.Woodcutting) > 0)
        assertNotEquals(tree.id, objects[tile].first().id)
    }

}