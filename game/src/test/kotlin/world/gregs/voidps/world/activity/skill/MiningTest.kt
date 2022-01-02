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

internal class MiningTest : WorldMock() {

    lateinit var objects: Objects

    @BeforeAll
    override fun setup() {
        super.setup()
        objects = get()
    }

    @Test
    fun `Mining gives ore and depletes`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("miner", Tile(100, 100))
        player.levels.setOffset(Skill.Mining, 100)
        val tile = Tile(100, 101)
        val rocks = createObject("tin_rocks_rock_1", tile)
        player.inventory.add("bronze_pickaxe")

        player.objectOption(rocks, "Mine")
        tickIf { player.inventory.spaces >= 27 }

        assertTrue(player.inventory.contains("tin_ore"))
        assertTrue(player.experience.get(Skill.Mining) > 0)
        assertNotEquals(rocks.id, objects[tile].first().id)
    }


}