package content.area.kharidian_desert.kalphite_lair

import WorldTest
import content.entity.effect.transform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class KalphiteQueenTest : WorldTest() {
    @Test
    fun `Kalphite Queen returns to original form after respawning`() {
        val queen = createNPC("kalphite_queen", emptyTile)
        queen.transform("kalphite_queen_airborne")

        queen.respawn(1)
        tick(2)

        assertEquals("", queen.transform)
        assertEquals("kalphite_queen", queen.transformId)
    }
}
