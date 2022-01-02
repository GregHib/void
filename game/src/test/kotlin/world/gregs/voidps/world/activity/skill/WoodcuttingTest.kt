package world.gregs.voidps.world.activity.skill

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.objectOption

internal class WoodcuttingTest : WorldMock() {

    lateinit var objects: Objects

    @BeforeAll
    override fun setup() {
        super.setup()
        objects = get()
    }

    @Test
    fun `Woodcutting gives log and depletes`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("jack", Tile(100, 100))
        player.levels.setOffset(Skill.Woodcutting, 100)
        val tile = Tile(100, 101)
        val tree = createObject("tree", tile)
        player.inventory.add("bronze_hatchet")

        player.objectOption(tree, "Chop down")
        tickIf { player.inventory.spaces >= 27 }

        assertTrue(player.inventory.contains("logs"))
        assertTrue(player.experience.get(Skill.Woodcutting) > 0)
        assertNotEquals(tree.id, objects[tile].first().id)
    }


}