package content.area.morytania.slayer_tower

import FakeRandom
import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlayerTowerTest : WorldTest() {

    @TestFactory
    fun `Climb spikey chain`() = mapOf(
        "up" to Tile(3422, 3550),
        "down" to Tile(3422, 3550, 1),
        "up" to Tile(3447, 3576, 1),
        "down" to Tile(3447, 3576, 2),
    ).map { (direction, tile) ->
        dynamicTest("Climb $direction spikey chain $tile") {
            val player = createPlayer(tile.addY(1))
            player.levels.set(Skill.Agility, 71)
            val chain = objects[tile, "slayer_tower_chain_$direction"]!!

            player.objectOption(chain, "Climb-$direction")

            tick(4)

            assertEquals(3.0, player.experience.get(Skill.Agility))
            assertEquals(tile.add(y = 1, level = if (direction == "up") 1 else -1), player.tile)
        }
    }

    @TestFactory
    fun `Can't climb medium spikey chain without level`() = mapOf(
        "up" to Tile(3422, 3550),
        "down" to Tile(3422, 3550, 1),
        "up" to Tile(3447, 3576, 1),
        "down" to Tile(3447, 3576, 2),
    ).map { (direction, tile) ->
        dynamicTest("Can't climb $direction spikey chain $tile without level") {
            val player = createPlayer(tile.addX(-1))
            player.levels.set(Skill.Agility, 60)
            val chain = objects[tile, "slayer_tower_chain_$direction"]!!

            player.objectOption(chain, "Climb-$direction")

            tick(2)

            assertTrue(player.containsMessage("You need an Agility level of"))
        }
    }

    @TestFactory
    fun `Fail to climb spikey chain does damage`() = mapOf(
        "up" to Tile(3422, 3550),
        "down" to Tile(3422, 3550, 1),
        "up" to Tile(3447, 3576, 1),
        "down" to Tile(3447, 3576, 2),
    ).map { (direction, tile) ->
        dynamicTest("Fail to climb $direction spikey chain $tile does damage") {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int) = 300
            })
            val player = createPlayer(tile.addY(1))
            player.levels.set(Skill.Agility, 99)
            val chain = objects[tile, "slayer_tower_chain_$direction"]!!

            player.objectOption(chain, "Climb-$direction")

            tick(6)

            assertEquals(6.0, player.experience.get(Skill.Agility))
            assertEquals(80, player.levels.get(Skill.Constitution))
            assertEquals(tile.add(y = 1, level = if (direction == "up") 1 else -1), player.tile)
        }
    }
}