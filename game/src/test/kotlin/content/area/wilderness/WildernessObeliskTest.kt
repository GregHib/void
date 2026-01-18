package content.area.wilderness

import WorldTest
import content.skill.magic.book.modern.teleBlock
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class WildernessObeliskTest : WorldTest() {

    @Test
    fun `Teleport using wilderness obelisk`() {
        val player = createPlayer(Tile(3154, 3619))
        val obj = objects.find(Tile(3154, 3618)) { it.id.startsWith("wilderness_obelisk") }
        player.objectOption(obj, "Activate")
        tick(1)
        player.walkTo(player.tile.addX(1))
        tick(11)
        assertEquals(Tile(3306, 3915), player.tile)
    }

    @Test
    fun `Can't use obelisk to teleport if tele blocked`() {
        val player = createPlayer(Tile(3154, 3619))
        val obj = objects.find(Tile(3154, 3618)) { it.id.startsWith("wilderness_obelisk") }
        player.objectOption(obj, "Activate")
        tick(1)
        player.teleBlock(player, 20)
        player.walkTo(player.tile.addX(1))
        tick(11)
        assertEquals(Tile(3155, 3619), player.tile)
    }
}
