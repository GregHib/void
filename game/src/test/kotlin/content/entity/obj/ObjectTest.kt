package content.entity.obj

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.WalkHandler
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Tile
import WorldTest
import objectOption

internal class ObjectTest : WorldTest() {

    private lateinit var collision: Collisions
    private val handler = WalkHandler()


    @BeforeEach
    fun start() {
        collision = get()
    }

    @Test
    fun `Can't walk through a door`() {
        val player = createPlayer("player", Tile(3227, 3214))
        tick()

        handler.validate(player, Walk(3226, 3214))
        tick(1)

        assertEquals(Tile(3227, 3214), player.tile)
    }

    @Test
    fun `Can open and walk through a door`() {
        val player = createPlayer("player", Tile(3227, 3214))
        tick()
        val door = objects.getLayer(Tile(3226, 3214), ObjectLayer.WALL)!!

        player.objectOption(door, "Open")
        tick()
        handler.validate(player, Walk(3226, 3214))
        tick(2)

        assertEquals(Tile(3226, 3214), player.tile)
    }

    @Test
    fun `Ladder ascending`() {
        val player = createPlayer("player", Tile(3229, 3214))
        tick()
        val ladder = objects.getLayer(Tile(3229, 3213), ObjectLayer.GROUND)!!

        player.objectOption(ladder, "Climb-up")
        tick(4)

        assertEquals(1, player.tile.level)
    }

    @Test
    fun `Ladder descending`() {
        val player = createPlayer("player", Tile(3229, 3214, 1))
        tick()

        // The one in Objects has wrong id as config replace id disabled.
        val ladder = GameObject(id = 36769, tile = Tile(3229, 3213, 1), shape = ObjectShape.GROUND_DECOR, rotation = 3)
        player.objectOption(ladder, "Climb-down")
        tick(4)

        assertEquals(0, player.tile.level)
    }

}