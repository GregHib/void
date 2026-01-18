package content.area.misthalin.barbarian_village

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class StrongholdOfSecurityPortalTest : WorldTest() {

    @Test
    fun `War portal teleport with experience`() {
        val tile = Tile(1863, 5239)
        val player = createPlayer(tile)
        player.combatLevel = 26
        val portal = objects.find(Tile(1863, 5238), "stronghold_war_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertNotEquals(tile, player.tile)
    }

    @Test
    fun `War portal teleport with completion`() {
        val tile = Tile(1863, 5239)
        val player = createPlayer(tile)
        player["unlocked_emote_flap"] = true
        val portal = objects.find(Tile(1863, 5238), "stronghold_war_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertNotEquals(tile, player.tile)
    }

    @Test
    fun `War portal can't teleport`() {
        val tile = Tile(1863, 5239)
        val player = createPlayer(tile)
        val portal = objects.find(Tile(1863, 5238), "stronghold_war_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertEquals(tile, player.tile)
    }

    @Test
    fun `Famine portal teleport with experience`() {
        val tile = Tile(2040, 5240)
        val player = createPlayer(tile)
        player.combatLevel = 51
        val portal = objects.find(Tile(2039, 5240), "stronghold_famine_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertNotEquals(tile, player.tile)
    }

    @Test
    fun `Famine portal teleport with completion`() {
        val tile = Tile(2040, 5240)
        val player = createPlayer(tile)
        player["unlocked_emote_slap_head"] = true
        val portal = objects.find(Tile(2039, 5240), "stronghold_famine_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertNotEquals(tile, player.tile)
    }

    @Test
    fun `Famine portal can't teleport`() {
        val tile = Tile(2040, 5240)
        val player = createPlayer(tile)
        val portal = objects.find(Tile(2039, 5240), "stronghold_famine_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertEquals(tile, player.tile)
    }

    @Test
    fun `Pestilence portal teleport with experience`() {
        val tile = Tile(2120, 5257)
        val player = createPlayer(tile)
        player.combatLevel = 76
        val portal = objects.find(Tile(2120, 5258), "stronghold_pestilence_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertNotEquals(tile, player.tile)
    }

    @Test
    fun `Pestilence portal teleport with completion`() {
        val tile = Tile(2120, 5257)
        val player = createPlayer(tile)
        player["unlocked_emote_idea"] = true
        val portal = objects.find(Tile(2120, 5258), "stronghold_pestilence_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertNotEquals(tile, player.tile)
    }

    @Test
    fun `Pestilence portal can't teleport`() {
        val tile = Tile(2120, 5257)
        val player = createPlayer(tile)
        val portal = objects.find(Tile(2120, 5258), "stronghold_pestilence_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertEquals(tile, player.tile)
    }

    @Test
    fun `Death portal teleport with completion`() {
        val tile = Tile(2364, 5212)
        val player = createPlayer(tile)
        player["unlocked_emote_stomp"] = true
        val portal = objects.find(Tile(2365, 5212), "stronghold_death_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertNotEquals(tile, player.tile)
    }

    @Test
    fun `Death portal can't teleport`() {
        val tile = Tile(2364, 5212)
        val player = createPlayer(tile)
        player.combatLevel = 128
        val portal = objects.find(Tile(2365, 5212), "stronghold_death_portal")

        player.objectOption(portal, "Enter")
        tick(2)

        assertEquals(tile, player.tile)
    }
}
