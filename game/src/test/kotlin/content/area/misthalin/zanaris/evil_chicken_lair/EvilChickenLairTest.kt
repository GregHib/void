package content.area.misthalin.zanaris.evil_chicken_lair

import WorldTest
import itemOnObject
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EvilChickenLairTest : WorldTest() {

    @Test
    fun `Raw chicken on the shrine teleports into the lair`() {
        val player = createPlayer(Tile(2452, 4476))
        player.inventory.add("raw_chicken")

        val shrine = GameObjects.find(Tile(2452, 4477), "chicken_shrine")
        player.itemOnObject(shrine, player.inventory.indexOf("raw_chicken"))
        tick(10)

        assertEquals(0, player.inventory.count("raw_chicken"))
        assertEquals(Tile(1563, 4357), player.tile)
    }

    @Test
    fun `Shrine teleport waits until the player stands in front of the shrine`() {
        val player = createPlayer(Tile(2451, 4477))
        player.inventory.add("raw_chicken")

        val shrine = GameObjects.find(Tile(2452, 4477), "chicken_shrine")
        player.itemOnObject(shrine, player.inventory.indexOf("raw_chicken"))
        tick()

        assertEquals(1, player.inventory.count("raw_chicken"), "Teleport started before reaching the shrine")

        tick(10)
        assertEquals(Tile(1563, 4357), player.tile)
    }

    @Test
    fun `Cooked chicken does nothing to the shrine`() {
        val player = createPlayer(Tile(2452, 4476))
        player.inventory.add("cooked_chicken")

        val shrine = GameObjects.find(Tile(2452, 4477), "chicken_shrine")
        player.itemOnObject(shrine, player.inventory.indexOf("cooked_chicken"))
        tick(4)

        assertEquals(1, player.inventory.count("cooked_chicken"))
        assertEquals(Tile(2452, 4476), player.tile)
    }

    @Test
    fun `Portal teleports out of the lair`() {
        val player = createPlayer(Tile(1563, 4357))

        val portal = GameObjects.find(Tile(1563, 4354), "evil_chicken_lair_portal")
        player.interactObject(portal, "Enter")
        tick(10)

        assertEquals(Tile(2452, 4476), player.tile)
    }

    @Test
    fun `Rope on the tunnel entrance allows climbing down and back up`() {
        val player = createPlayer(Tile(1561, 4380))
        player.inventory.add("rope")

        val tunnel = GameObjects.find(Tile(1559, 4380), "evil_chicken_lair_tunnel_entrance")
        player.itemOnObject(tunnel, player.inventory.indexOf("rope"))
        tick(2)

        assertEquals(0, player.inventory.count("rope"))
        assertNull(GameObjects.findOrNull(Tile(1559, 4380), "evil_chicken_lair_tunnel_entrance"))
        val roped = GameObjects.find(Tile(1559, 4380), "evil_chicken_lair_tunnel_entrance_rope")
        assertNotNull(roped)

        player.interactObject(roped, "Climb-down")
        tick(10)
        assertEquals(Tile(1545, 4382), player.tile)

        val rope = GameObjects.find(Tile(1545, 4381), "evil_chicken_lair_rope")
        player.interactObject(rope, "Climb-up")
        tick(10)
        assertEquals(Tile(1561, 4380), player.tile)
    }
}
