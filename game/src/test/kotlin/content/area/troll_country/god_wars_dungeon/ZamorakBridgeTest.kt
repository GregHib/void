package content.area.troll_country.god_wars_dungeon

import WorldTest
import containsMessage
import messages
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZamorakBridgeTest : WorldTest() {

    @Test
    fun `Climb off bridge north`() {
        val player = createPlayer(Tile(2885, 5332, 2))
        player.levels.set(Skill.Constitution, 700)
        player.levels.set(Skill.Prayer, 50)
        val bridge = objects[Tile(2885, 5333, 2), "godwars_zamorak_bridge"]!!

        player.objectOption(bridge, "Climb-off")
        tick(8)

        assertEquals(0, player.levels.get(Skill.Prayer))
        assertEquals(Tile(2885, 5345, 2), player.tile)
    }

    @Test
    fun `Climb off bridge south`() {
        val player = createPlayer(Tile(2885, 5345, 2))
        player.levels.set(Skill.Constitution, 700)
        player.levels.set(Skill.Prayer, 50)
        val bridge = objects[Tile(2885, 5344, 2), "godwars_zamorak_bridge"]!!

        player.objectOption(bridge, "Climb-off")
        tick(8)


        assertEquals(50, player.levels.get(Skill.Prayer))
        assertEquals(Tile(2885, 5332, 2), player.tile)
    }

    @Test
    fun `Can't climb bridge without level 70 hp`() {
        val player = createPlayer(Tile(2885, 5332, 2))
        player.levels.set(Skill.Constitution, 690)
        val bridge = objects[Tile(2885, 5333, 2), "godwars_zamorak_bridge"]!!

        player.objectOption(bridge, "Climb-off")
        tick(2)

        assertTrue(player.containsMessage("You need to have a Constitution level of 70"))
        assertEquals(Tile(2885, 5332, 2), player.tile)
    }

}