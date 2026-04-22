package content.entity.effect

import WorldTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class FreezeTest : WorldTest() {

    @Test
    fun `Frozen players can't move or interact`() {
        val player = createPlayer(Tile(3229, 3215))

        player.freeze(10)
        tick(1)

        player.walkTo(Tile(3228, 3215))
        tick(4)
        assertEquals(Tile(3229, 3215), player.tile)

        val ladder = GameObjects.find(Tile(3229, 3213), "lumbridge_tower_ladder_south")
        player.interactObject(ladder, "Climb-up")
        tick(4)
        assertEquals(Tile(3229, 3215), player.tile)
    }
}
