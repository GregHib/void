package world.gregs.voidps.world.interact.entity.player.effect

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.world.interact.entity.player.effect.Dragonfire.maxHit

internal class DragonfireTest {

    @Test
    fun `King black dragon`() {
        val type = "king_black_dragon"
        assertEquals(650, maxHit(type, special = false, shield = false, protection = false, potion = 0))
        assertEquals(500, maxHit(type, special = true, shield = false, protection = false, potion = 0))

        assertEquals(150, maxHit(type, special = false, shield = true, protection = false, potion = 0))
        assertEquals(100, maxHit(type, special = true, shield = true, protection = false, potion = 0))

        assertEquals(200, maxHit(type, special = false, shield = false, protection = true, potion = 0))
        assertEquals(150, maxHit(type, special = true, shield = false, protection = true, potion = 0))

        assertEquals(500, maxHit(type, special = false, shield = false, protection = false, potion = 1))
        assertEquals(500, maxHit(type, special = true, shield = false, protection = false, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = false, protection = false, potion = 2))
        assertEquals(500, maxHit(type, special = true, shield = false, protection = false, potion = 2))

        assertEquals(150, maxHit(type, special = false, shield = true, protection = true, potion = 0))
        assertEquals(100, maxHit(type, special = true, shield = true, protection = true, potion = 0))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = false, potion = 1))
        assertEquals(100, maxHit(type, special = true, shield = true, protection = false, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = false, potion = 2))
        assertEquals(100, maxHit(type, special = true, shield = true, protection = false, potion = 2))

        assertEquals(50, maxHit(type, special = false, shield = false, protection = true, potion = 1))
        assertEquals(150, maxHit(type, special = true, shield = false, protection = true, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = false, protection = true, potion = 2))
        assertEquals(150, maxHit(type, special = true, shield = false, protection = true, potion = 2))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = true, potion = 1))
        assertEquals(100, maxHit(type, special = true, shield = true, protection = true, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = true, potion = 2))
        assertEquals(100, maxHit(type, special = true, shield = true, protection = true, potion = 2))
    }

    @Test
    fun `Chromatic dragon`() {
        val type = "chromatic"
        assertEquals(300, maxHit(type, special = false, shield = false, protection = false, potion = 0))
        assertEquals(500, maxHit(type, special = true, shield = false, protection = false, potion = 0))

        assertEquals(50, maxHit(type, special = false, shield = true, protection = false, potion = 0))
        assertEquals(50, maxHit(type, special = true, shield = true, protection = false, potion = 0))

        assertEquals(100, maxHit(type, special = false, shield = false, protection = true, potion = 0))
        assertEquals(100, maxHit(type, special = true, shield = false, protection = true, potion = 0))

        assertEquals(150, maxHit(type, special = false, shield = false, protection = false, potion = 1))
        assertEquals(350, maxHit(type, special = true, shield = false, protection = false, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = false, protection = false, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = false, protection = false, potion = 2))

        assertEquals(50, maxHit(type, special = false, shield = true, protection = true, potion = 0))
        assertEquals(50, maxHit(type, special = true, shield = true, protection = true, potion = 0))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = false, potion = 1))
        assertEquals(0, maxHit(type, special = true, shield = true, protection = false, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = false, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = true, protection = false, potion = 2))

        assertEquals(0, maxHit(type, special = false, shield = false, protection = true, potion = 1))
        assertEquals(0, maxHit(type, special = true, shield = false, protection = true, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = false, protection = true, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = false, protection = true, potion = 2))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = true, potion = 1))
        assertEquals(0, maxHit(type, special = true, shield = true, protection = true, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = true, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = true, protection = true, potion = 2))
    }

    @Test
    fun `Metallic dragon`() {
        val type = "metallic"
        assertEquals(300, maxHit(type, special = false, shield = false, protection = false, potion = 0))
        assertEquals(500, maxHit(type, special = true, shield = false, protection = false, potion = 0))

        assertEquals(50, maxHit(type, special = false, shield = true, protection = false, potion = 0))
        assertEquals(50, maxHit(type, special = true, shield = true, protection = false, potion = 0))

        assertEquals(300, maxHit(type, special = false, shield = false, protection = true, potion = 0))
        assertEquals(500, maxHit(type, special = true, shield = false, protection = true, potion = 0))

        assertEquals(150, maxHit(type, special = false, shield = false, protection = false, potion = 1))
        assertEquals(350, maxHit(type, special = true, shield = false, protection = false, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = false, protection = false, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = false, protection = false, potion = 2))

        assertEquals(50, maxHit(type, special = false, shield = true, protection = true, potion = 0))
        assertEquals(50, maxHit(type, special = true, shield = true, protection = true, potion = 0))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = false, potion = 1))
        assertEquals(0, maxHit(type, special = true, shield = true, protection = false, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = false, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = true, protection = false, potion = 2))

        assertEquals(150, maxHit(type, special = false, shield = false, protection = true, potion = 1))
        assertEquals(350, maxHit(type, special = true, shield = false, protection = true, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = false, protection = true, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = false, protection = true, potion = 2))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = true, potion = 1))
        assertEquals(0, maxHit(type, special = true, shield = true, protection = true, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = true, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = true, protection = true, potion = 2))
    }

    @Test
    fun `Elvarg dragon`() {
        val type = "elvarg"
        assertEquals(700, maxHit(type, special = false, shield = false, protection = false, potion = 0))
        assertEquals(700, maxHit(type, special = true, shield = false, protection = false, potion = 0))

        assertEquals(100, maxHit(type, special = false, shield = true, protection = false, potion = 0))
        assertEquals(100, maxHit(type, special = true, shield = true, protection = false, potion = 0))

        assertEquals(550, maxHit(type, special = false, shield = false, protection = true, potion = 0))
        assertEquals(550, maxHit(type, special = true, shield = false, protection = true, potion = 0))

        assertEquals(550, maxHit(type, special = false, shield = false, protection = false, potion = 1))
        assertEquals(550, maxHit(type, special = true, shield = false, protection = false, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = false, protection = false, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = false, protection = false, potion = 2))

        assertEquals(70, maxHit(type, special = false, shield = true, protection = true, potion = 0))
        assertEquals(70, maxHit(type, special = true, shield = true, protection = true, potion = 0))

        assertEquals(70, maxHit(type, special = false, shield = true, protection = false, potion = 1))
        assertEquals(70, maxHit(type, special = true, shield = true, protection = false, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = false, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = true, protection = false, potion = 2))

        assertEquals(400, maxHit(type, special = false, shield = false, protection = true, potion = 1))
        assertEquals(400, maxHit(type, special = true, shield = false, protection = true, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = false, protection = true, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = false, protection = true, potion = 2))

        assertEquals(40, maxHit(type, special = false, shield = true, protection = true, potion = 1))
        assertEquals(40, maxHit(type, special = true, shield = true, protection = true, potion = 1))

        assertEquals(0, maxHit(type, special = false, shield = true, protection = true, potion = 2))
        assertEquals(0, maxHit(type, special = true, shield = true, protection = true, potion = 2))
    }

}