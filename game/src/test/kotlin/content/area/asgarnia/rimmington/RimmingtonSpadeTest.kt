package content.area.asgarnia.rimmington

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class RimmingtonSpadeTest : WorldTest() {

    @Test
    fun `Taking the Rimmington spade stops one tile away, animates and respawns it`() {
        val tile = Tile(2979, 3241)
        val player = createPlayer(tile.addY(-3))
        val spade = GameObjects.findOrNull(tile, 9662) ?: error("Spade object 9662 missing at $tile")

        player.objectOption(spade, "Take")
        tickIf(limit = 10) { !player.inventory.contains("spade") }

        assertTrue(player.inventory.contains("spade"))
        assertNotEquals(tile, player.tile)
        assertEquals(1, player.tile.distanceTo(tile))
        assertEquals(AnimationDefinitions.get("take").id, player.visuals.animation.force)
        assertNull(GameObjects.findOrNull(tile, 9662))

        tick(73)
        assertNull(GameObjects.findOrNull(tile, 9662))

        tick()
        assertNotNull(GameObjects.findOrNull(tile, 9662))
    }

    @Test
    fun `Taking the Rimmington spade still works while standing under it`() {
        val tile = Tile(2979, 3241)
        val player = createPlayer(tile)
        val spade = GameObjects.findOrNull(tile, 9662) ?: error("Spade object 9662 missing at $tile")

        player.objectOption(spade, "Take")
        tickIf(limit = 5) { !player.inventory.contains("spade") }

        assertTrue(player.inventory.contains("spade"))
        assertEquals(tile, player.tile)
        assertEquals(AnimationDefinitions.get("take").id, player.visuals.animation.force)
    }

    @Test
    fun `Taking the Rimmington spade with a full inventory shows the expected message`() {
        val tile = Tile(2979, 3241)
        val player = createPlayer(tile.addY(-1))
        repeat(28) {
            player.inventory.add("bucket")
        }
        val spade = GameObjects.findOrNull(tile, 9662) ?: error("Spade object 9662 missing at $tile")

        player.objectOption(spade, "Take")
        tick()

        assertTrue(player.containsMessage("You haven't got room to hold that."))
        assertTrue(!player.inventory.contains("spade"))
        assertNotNull(GameObjects.findOrNull(tile, 9662))
    }
}
