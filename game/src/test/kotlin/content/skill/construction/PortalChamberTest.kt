package content.skill.construction

import WorldTest
import content.quest.instance
import content.skill.construction.House.Companion.START_ROOM
import content.skill.construction.House.Companion.addHouseFurniture
import content.skill.construction.House.Companion.addHouseRoom
import content.skill.construction.House.Companion.removeHouseFurniture
import content.skill.construction.House.Companion.roomZone
import dialogueOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PortalChamberTest : WorldTest() {

    private val portal = Tile(2951, 3222)
    private val exit = Tile(2953, 3224)

    private fun createOwner(direction: Int = 0): Player {
        val player = createPlayer(exit, "owner")
        player["house_location"] = "rimmington"
        player["house_portal"] = direction
        player.levels.set(Skill.Magic, 99)
        player.addHouseRoom("portal_chamber", START_ROOM)
        player.addHouseFurniture(START_ROOM, "portal_chamber_portal_space", "teak_portal")
        player.addHouseFurniture(START_ROOM, "portal_chamber_centrepiece_space", "teleport_focus")
        enterHouse(player)
        return player
    }

    private fun enterHouse(player: Player) {
        player.objectOption(GameObjects.find(portal, "house_portal_rimmington"), "Enter")
        tickIf { player.dialogue == null }
        player.dialogueOption("line1")
        tickIf { player.hasOpen("house_loading") }
    }

    private fun Player.objects(id: String): List<GameObject> {
        val zone = roomZone(instance()!!.tile.zone, START_ROOM)
        return zone.toCuboid().mapNotNull { GameObjects.findOrNull(it, id) }
    }

    @Test
    fun `Direct a portal`() {
        val player = createOwner()
        player.inventory.add("fire_rune", 100)
        player.inventory.add("air_rune", 300)
        player.inventory.add("law_rune", 100)

        player.objectOption(player.objects("teleport_focus").single(), "Direct-portal")
        tickIf { player.dialogue == null }
        player.dialogueOption("line1")

        assertEquals(1, player["house_portal", 0])
        assertTrue(player.inventory.isEmpty())
        assertEquals(1, player.objects("teak_portal_varrock").size)
        assertTrue(player.objects("teak_portal").isEmpty())
    }

    @Test
    fun `Can't direct a portal without enough runes`() {
        val player = createOwner()
        player.inventory.add("fire_rune", 99)
        player.inventory.add("air_rune", 300)
        player.inventory.add("law_rune", 100)

        player.objectOption(player.objects("teleport_focus").single(), "Direct-portal")
        tickIf { player.dialogue == null }
        player.dialogueOption("line1")

        assertEquals(0, player["house_portal", 0])
        assertEquals(300, player.inventory.count("air_rune"))
        assertEquals(1, player.objects("teak_portal").size)
    }

    @Test
    fun `Enter a directed portal`() {
        val player = createOwner(direction = 2)

        player.objectOption(player.objects("teak_portal_lumbridge").single(), "Enter")
        tick(6)

        assertTrue(player.tile in Areas["lumbridge_teleport"])
    }

    @Test
    fun `Removing a portal resets its direction`() {
        val player = createOwner(direction = 3)

        player.removeHouseFurniture(START_ROOM, "portal_chamber_portal_space")

        assertEquals(0, player["house_portal", 0])
    }
}
