package content.area.wilderness.daemonheim

import WorldTest
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import dialogueContinue
import dialogueOption
import interfaceOption
import itemOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import playerOption
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class DungeoneeringPartyTest : WorldTest() {

    @Test
    fun `Join a players party`() {
        val player1 = createPlayer(Tile(3449, 3725))
        val player2 = createPlayer(Tile(3449, 3726))

        player1.inventory.add("ring_of_kinship")
        player1.itemOption("Open party interface", "ring_of_kinship")
        player1.interfaceOption("dungeoneering_party", "form_party", "Form-party")

        player1.playerOption(player2, "Invite")
        player2.playerOption(player1, "Invite")
        tick(1)

        player2.interfaceOption("dungeon_party_invite", "accept", "Accept")

        assertEquals(listOf(player1, player2), player1.dungeonMembers)
        assertEquals(player1, player2.dungeonLeader)
        assertEquals(listOf(player1, player2), player2.dungeonMembers)

        player1.interfaceOption("dungeoneering_party", "leave_party", "Leave-party")
        assertNull(player1.dungeonLeader)
        assertEquals(player2, player2.dungeonLeader)
        assertEquals(listOf(player2), player2.dungeonMembers)
    }

    @Test
    fun `Select a dungeon floor in daemonheim`() {
        val player = createPlayer(Tile(3449, 3725))
        player["dungeoneering_floor"] = 10
        player.inventory.add("ring_of_kinship")

        player.itemOption("Open party interface", "ring_of_kinship")
        player.interfaceOption("dungeoneering_party", "form_party", "Form-party")
        player.interfaceOption("dungeoneering_party", "change_floor", "Change-floor")
        player.interfaceOption("dungeon_floor", "select_6", "Select-floor")
        player.interfaceOption("dungeon_floor", "confirm", "Confirm")

        assertEquals(6, player["dungeoneering_party_floor", 1])
    }

    @Test
    fun `Select a dungeon complexity in daemonheim`() {
        val player = createPlayer(Tile(3449, 3725))
        player["dungeoneering_complexity"] = 5
        player.inventory.add("ring_of_kinship")

        player.itemOption("Open party interface", "ring_of_kinship")
        player.interfaceOption("dungeoneering_party", "form_party", "Form-party")
        player.interfaceOption("dungeoneering_party", "change_complexity", "Change-complexity")
        player.interfaceOption("dungeon_complexity", "complexity_3", "Select-complexity")
        player.interfaceOption("dungeon_complexity", "confirm", "Confirm")

        assertEquals(3, player["dungeoneering_party_complexity", 1])
    }

    @Test
    fun `Toggle guide mode in daemonheim`() {
        val player = createPlayer(Tile(3449, 3725))
        player.inventory.add("ring_of_kinship")

        player.itemOption("Open party interface", "ring_of_kinship")
        player.interfaceOption("dungeoneering_party", "form_party", "Form-party")
        player.interfaceOption("dungeoneering_party", "guide_mode", "Toggle")

        assertTrue(player["dungeoneering_guide_mode", false])
    }

    @Test
    fun `Reset progress`() {
        val player = createPlayer(Tile(3449, 3725))
        player["dungeoneering_current_progress"] = 5
        player["dungeoneering_previous_progress"] = 10
        player.inventory.add("ring_of_kinship")

        player.itemOption("Open party interface", "ring_of_kinship")
        player.interfaceOption("dungeoneering_party", "reset", "Reset")
        player.dialogueContinue()
        player.dialogueOption("line1")

        assertEquals(1, player["dungeoneering_current_progress", 1])
        assertEquals(5, player["dungeoneering_previous_progress", 1])
    }

    @Test
    fun `Exit a party`() {
        val player = createPlayer(Tile(3449, 3725))
        player["dungeoneering_complexity"] = 5
        player.inventory.add("ring_of_kinship")

        player.itemOption("Open party interface", "ring_of_kinship")
        player.interfaceOption("dungeoneering_party", "form_party", "Form-party")
        assertEquals(player, player.dungeonLeader)
        player.interfaceOption("dungeoneering_party", "leave_party", "Leave-party")
        assertNull(player.dungeonLeader)
    }
}
