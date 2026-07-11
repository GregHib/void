package content.activity.event.random

import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LostAndFoundTest : WorldTest() {

    private val origin = Tile(3221, 3218)
    private val plane = Tile(2332, 4770)
    private val appendages = listOf(
        Tile(2336, 4771),
        Tile(2332, 4775),
        Tile(2327, 4771),
        Tile(2332, 4766),
    )

    private fun enter(name: String, tile: Tile = origin): Player {
        val player = createPlayer(tile, name)
        RandomEvents.start(player, "lost_and_found")
        tick(6)
        return player
    }

    private fun Player.operate(tile: Tile) {
        val appendage = GameObjects.getShape(tile, 10)!!
        objectOption(appendage, "Operate")
        tick(8)
    }

    private fun Player.oddTile() = appendages[get("laf_odd", 0) - 1]

    private fun Player.wrongTile() = appendages.first { it != oddTile() }

    @Test
    fun `The player slips through to the Abyssal plane`() {
        val player = enter("laf_start")

        assertEquals("lost_and_found", player.get<String>("random_event"))
        assertEquals(plane, player.tile)
        assertTrue(player.get("laf_odd", 0) in 1..4)
    }

    @Test
    fun `Three appendages match and the odd one differs`() {
        val player = enter("laf_shapes")

        val shapes = (1..4).map { player.get("lost_and_found_appendage_$it", -1) }
        val odd = player.get("laf_odd", 0)

        assertEquals(1, shapes.count { it == shapes[odd - 1] })
        assertEquals(3, shapes.count { it != shapes[odd - 1] })
        assertEquals(1, shapes.distinct().size - 1)
    }

    @Test
    fun `The wrong appendage drains Magic and reshuffles`() {
        val player = enter("laf_wrong")
        player.levels.set(Skill.Magic, 50)

        player.operate(player.wrongTile())

        assertEquals("lost_and_found", player.get<String>("random_event"))
        assertTrue(player.levels.get(Skill.Magic) < 50)
    }

    @Test
    fun `The odd appendage forwards the player home`() {
        val player = enter("laf_right")

        player.operate(player.oddTile())

        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
        assertEquals(1, player.inventory.count("random_event_gift"))
    }
}
