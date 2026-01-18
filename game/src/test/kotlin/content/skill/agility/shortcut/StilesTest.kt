package content.skill.agility.shortcut

import WorldTest
import objectOption
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

internal class StilesTest : WorldTest() {

    @TestFactory
    fun `Climb east facing stile`() = listOf(
        Delta(0, 0) to Tile(2648, 3375),
        Delta(-1, 0) to Tile(2648, 3375),
        Delta(0, -1) to Tile(2648, 3375),
        Delta(0, 1) to Tile(2648, 3375),
        Delta(1, 0) to Tile(2647, 3375),
        Delta(2, 0) to Tile(2647, 3375),
        Delta(1, -1) to Tile(2647, 3375),
        Delta(1, 1) to Tile(2647, 3375),
    ).map { (delta, target) ->
        dynamicTest("Climb east stile from $delta to $target") {
            val objTile = Tile(2647, 3375)
            val obj = GameObjects.find(objTile, "ardougne_farm_stile")
            val player = createPlayer(objTile.add(delta))

            player.objectOption(obj, "Climb-over")

            tick(4)

            assertEquals(target, player.tile)
        }
    }

    @TestFactory
    fun `Climb north facing stile`() = listOf(
        Delta(0, 0) to Tile(3197, 3277),
        Delta(0, -1) to Tile(3197, 3277),
        Delta(-1, 0) to Tile(3197, 3277),
        Delta(1, 0) to Tile(3197, 3277),
        Delta(0, 1) to Tile(3197, 3276),
        Delta(0, 2) to Tile(3197, 3276),
        Delta(-1, 1) to Tile(3197, 3276),
        Delta(1, 1) to Tile(3197, 3276),
    ).map { (delta, target) ->
        dynamicTest("Climb north stile from $delta to $target") {
            val objTile = Tile(3197, 3276)
            val obj = GameObjects.find(objTile, "freds_farm_stile")
            val player = createPlayer(objTile.add(delta))

            player.objectOption(obj, "Climb-over")

            tick(4)

            assertEquals(target, player.tile)
        }
    }
}
