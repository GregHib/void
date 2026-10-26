package content.area.misthalin.dorgesh_kaan

import WorldTest
import itemOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MolaniskTest : WorldTest() {

    private val wall = Tile(2730, 5205)

    @Test
    fun `Ringing a slayer bell lures a molanisk off the wall`() {
        val player = createPlayer(wall.addY(-1))
        player.levels.set(Skill.Slayer, 39)
        player.inventory.add("slayer_bell")

        player.itemOption("Ring", "slayer_bell")
        tick()

        assertEquals(1, NPCs.count { it.id == "molanisk" && it["owner", ""] == player.accountName })
        assertNotNull(GameObjects.at(wall).firstOrNull { it.id == "molanisk_wall_empty" })
    }

    @Test
    fun `Ringing a slayer bell below 39 slayer does nothing`() {
        val player = createPlayer(wall.addY(-1))
        player.inventory.add("slayer_bell")

        player.itemOption("Ring", "slayer_bell")
        tick()

        assertEquals(0, NPCs.count { it.id == "molanisk" })
        assertNull(GameObjects.at(wall).firstOrNull { it.id == "molanisk_wall_empty" })
    }
}
