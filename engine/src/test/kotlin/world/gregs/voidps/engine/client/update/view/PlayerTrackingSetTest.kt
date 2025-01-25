package world.gregs.voidps.engine.client.update.view

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class PlayerTrackingSetTest {
    lateinit var set: PlayerTrackingSet
    lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = Player(index = 1)
        set = PlayerTrackingSet()
    }

    @Test
    fun `Doesn't need appearance after update`() {
        assertTrue(set.needsAppearanceUpdate(player))
        set.updateAppearance(player)
        assertFalse(set.needsAppearanceUpdate(player))
    }

    @Test
    fun `Add self to local players`() {
        set.addSelf(player)
        set.update()

        assertEquals(1, set.localCount)
        assertEquals(1, set.locals[0])
        assertEquals(2046, set.globalCount)
    }

    @Test
    fun `Add global to local player list`() {
        assertEquals(0, set.localCount)

        set.add(2)
        set.update()
        assertEquals(1, set.localCount)
        assertEquals(2, set.locals[0])
        assertEquals(2046, set.globalCount)
    }

    @Test
    fun `Remove from local player list`() {
        set.locals[set.localCount++] = 1

        set.remove(1)
        set.update()
        assertEquals(0, set.localCount)
        assertEquals(2047, set.globalCount)
    }

    @Test
    fun `Two locals are added in order`() {
        set.add(3)
        set.add(2)
        set.update()

        assertEquals(2, set.localCount)
        assertEquals(2, set.locals[0])
        assertEquals(3, set.locals[1])
        assertEquals(2045, set.globalCount)
    }
}
