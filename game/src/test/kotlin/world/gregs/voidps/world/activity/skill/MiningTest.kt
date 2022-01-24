package world.gregs.voidps.world.activity.skill

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.objectOption

internal class MiningTest : WorldTest() {

    lateinit var objects: Objects

    @BeforeEach
    fun start() {
        objects = get()
    }

    @Test
    fun `Mining gives ore and depletes`() {
        val player = createPlayer("miner", emptyTile)
        player.levels.setOffset(Skill.Mining, 100)
        val tile = emptyTile.addY(1)
        val rocks = createObject("tin_rocks_rock_1", tile)
        player.inventory.add("bronze_pickaxe")

        player.objectOption(rocks, "Mine")
        tickIf { player.inventory.spaces >= 27 }

        assertTrue(player.inventory.contains("tin_ore"))
        assertTrue(player.experience.get(Skill.Mining) > 0)
        assertNotEquals(rocks.id, objects[tile].first().id)
    }


}