package content.entity.obj

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class BushPickingTest : WorldTest() {

    @Test
    fun `Pick cadava bush`() {
        val player = createPlayer(Tile(3268, 3369))
        val bush = objects[Tile(3268, 3370), "cadava_bush_full"]!!

        player.objectOption(bush, "Pick-from")
        tick()

        assertNotNull(objects[Tile(3268, 3370), "cadava_bush_half"])
        assertTrue(player.inventory.contains("cadava_berries"))
    }

    @Test
    fun `Can't pick from empty cadava bush`() {
        val player = createPlayer(Tile(3268, 3369))
        var bush = objects[Tile(3268, 3370), "cadava_bush_full"]!!

        player.objectOption(bush, "Pick-from")
        tick()

        bush = objects[Tile(3268, 3370), "cadava_bush_half"]!!
        player.objectOption(bush, "Pick-from")
        tick()

        assertNotNull(objects[Tile(3268, 3370), "cadava_bush_empty"])
        assertEquals(2, player.inventory.count("cadava_berries"))
    }

    @Test
    fun `Pick redberry bush`() {
        val player = createPlayer(Tile(3277, 3370))
        val bush = objects[Tile(3277, 3371), "redberry_bush_full"]!!

        player.objectOption(bush, "Pick-from")
        tick()

        assertNotNull(objects[Tile(3277, 3371), "redberry_bush_half"])
        assertTrue(player.inventory.contains("redberries"))
    }

    @Test
    fun `Can't pick from empty redberry bush`() {
        val player = createPlayer(Tile(3277, 3370))
        var bush = objects[Tile(3277, 3371), "redberry_bush_full"]!!

        player.objectOption(bush, "Pick-from")
        tick()

        bush = objects[Tile(3277, 3371), "redberry_bush_half"]!!
        player.objectOption(bush, "Pick-from")
        tick()

        assertNotNull(objects[Tile(3277, 3371), "redberry_bush_empty"])
        assertEquals(2, player.inventory.count("redberries"))
    }
}