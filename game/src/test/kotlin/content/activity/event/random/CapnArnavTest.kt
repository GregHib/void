package content.activity.event.random

import WorldTest
import interfaceOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CapnArnavTest : WorldTest() {

    override var loadNpcs: Boolean = true

    private val origin = Tile(3221, 3218)
    private val island = Tile(1626, 5163)
    private val words = listOf("Coins", "Bowl", "Bar", "Ring")

    private fun enter(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "capn_arnav")
        tick(8)
        return player
    }

    private fun Player.openLock() {
        val chest = GameObjects.getShape(Tile(1627, 5162), 10)!!
        objectOption(chest, "Open")
        tick(2)
    }

    private fun Player.solveColumns() {
        val presses = words.indexOf(get("arnav_target", ""))
        for (column in 1..3) {
            repeat(presses) {
                interfaceOption("capn_arnav_lock", "up_$column", "Up")
                tick()
            }
        }
    }

    private fun Player.unlock() {
        interfaceOption("capn_arnav_lock", "unlock", "Unlock")
        tick(2)
        skipDialogues()
        tick()
    }

    @Test
    fun `Cap'n Arnav kidnaps the player to his island`() {
        val player = enter("arnav_start")

        assertEquals("capn_arnav", player.get<String>("random_event"))
        assertEquals(island, player.tile)
    }

    @Test
    fun `Opening the chest picks a target and opens the lock`() {
        val player = enter("arnav_lock")

        player.openLock()

        assertTrue(player.get("arnav_target", "") in words)
        assertEquals(3, (1..3).count { player.get("arnav_lock_$it", 0) == 0 })
    }

    @Test
    fun `The right combination opens the chest for a gift`() {
        val player = enter("arnav_solve")
        player.openLock()

        player.solveColumns()
        player.unlock()

        assertTrue(player.get("arnav_solved", false))
        assertEquals(1, player.inventory.count("random_event_gift"))
    }

    @Test
    fun `A full inventory defers the gift to the trip home instead of dropping it on the island`() {
        val player = enter("arnav_full_inv")
        while (!player.inventory.isFull()) {
            player.inventory.add("logs")
        }
        player.openLock()
        player.solveColumns()
        player.unlock()

        assertTrue(player.get("arnav_solved", false))
        assertTrue(player.get("arnav_gift_owed", false))
        assertFalse(player.inventory.contains("random_event_gift"))

        val portal = GameObjects.getShape(Tile(1626, 5165), 10)!!
        player.objectOption(portal, "Enter")
        tick(8)

        assertEquals(origin, player.tile)
        assertNotNull(FloorItems.firstOrNull(origin, "random_event_gift"), "Gift should land at the player's feet back home")
    }

    @Test
    fun `A wrong combination counts a try`() {
        val player = enter("arnav_wrong")
        player.openLock()

        player.interfaceOption("capn_arnav_lock", "up_1", "Up")
        tick()
        player.unlock()

        assertEquals(1, player.get("arnav_tries", 0))
        assertEquals("capn_arnav", player.get<String>("random_event"))
        assertFalse(player.get("arnav_solved", false))
    }

    @Test
    fun `Five wrong combinations get the player clobbered and exiled`() {
        val player = enter("arnav_fail")

        repeat(5) {
            player.openLock()
            player.interfaceOption("capn_arnav_lock", "up_1", "Up")
            tick()
            player.unlock()
        }
        tick(2)

        assertNull(player.get<String>("random_event"))
        assertNotEquals(island, player.tile)
        assertNotEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
        assertTrue(player.inventory.isEmpty())
    }

    @Test
    fun `The exit portal returns the player home after solving`() {
        val player = enter("arnav_finish")
        player.openLock()
        player.solveColumns()
        player.unlock()

        val portal = GameObjects.getShape(Tile(1626, 5165), 10)!!
        player.objectOption(portal, "Enter")
        tick(8) // walk to the portal + the modern teleport takeoff delay

        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertEquals(1, player.inventory.count("random_event_gift"))
        assertTrue(player.contains("random_event_cooldown"))
    }
}
