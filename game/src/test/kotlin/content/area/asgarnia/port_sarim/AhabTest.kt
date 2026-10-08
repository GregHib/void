package content.area.asgarnia.port_sarim

import WorldTest
import dialogueContinue
import dialogueOption
import floorItemOption
import interfaceOnFloorItem
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertNull

class AhabTest : WorldTest() {

    @Test
    fun `Talk-to can ask about Redbeard Frank before Pirate's Treasure is complete`() {
        val player = createPlayer(Tile(3049, 3256))
        val ahab = createNPC("ahab_port_sarim", Tile(3049, 3257))

        openAhabMenu(player, ahab)
        player.dialogueOption("line1")
        player.skipDialogues()

        assertNull(player.dialogue)
    }

    @Test
    fun `Talk-to can discuss getting another ship after Dragon Slayer`() {
        val player = createPlayer(Tile(3049, 3256))
        player["dragon_slayer"] = "completed"
        val ahab = createNPC("ahab_port_sarim", Tile(3049, 3257))

        openAhabMenu(player, ahab)
        player.dialogueOption("line3")
        player.skipDialogues()

        assertNull(player.dialogue)
    }

    @Test
    fun `Talk-to still offers trade after Pirate's Treasure is complete`() {
        val player = createPlayer(Tile(3049, 3256))
        player["pirates_treasure"] = "completed"
        val ahab = createNPC("ahab_port_sarim", Tile(3049, 3257))

        openAhabMenu(player, ahab)
        player.dialogueOption("line3")
        player.skipDialogues()

        assertNull(player.dialogue)
    }

    @Test
    fun `Can't take Ahab's beer`() {
        val player = createPlayer(Tile(3049, 3256))
        val beer = createFloorItem("ahabs_beer", Tile(3049, 3257))

        player.floorItemOption(beer, "Take")
        tickIf(10) { player.dialogue == null }

        assertFalse(player.inventory.contains("ahabs_beer"))
        assertTrue(FloorItems.at(beer.tile).any { it.id == "ahabs_beer" })
        assertEquals("dialogue_npc_chat1", player.dialogue)

        player.dialogueContinue()

        assertEquals("dialogue_chat1", player.dialogue)

        player.dialogueContinue()

        assertNull(player.dialogue)
    }

    @Test
    fun `Can't telegrab Ahab's beer`() {
        val player = createPlayer(Tile(3045, 3257))
        player.levels.set(Skill.Magic, 99)
        player.inventory.add("law_rune")
        player.inventory.add("air_rune")
        val beer = createFloorItem("ahabs_beer", Tile(3049, 3257))
        tick()

        player.interfaceOnFloorItem("modern_spellbook", "telekinetic_grab", beer)
        tickIf(10) { player.dialogue == null }

        assertFalse(player.inventory.contains("ahabs_beer"))
        assertTrue(FloorItems.at(beer.tile).any { it.id == "ahabs_beer" })
        assertEquals("dialogue_npc_chat1", player.dialogue)

        player.dialogueContinue()

        assertNull(player.dialogue)
    }

    private fun openAhabMenu(player: Player, ahab: NPC) {
        player.npcOption(ahab, "Talk-to")
        tickIf(10) { player.dialogue == null }
        player.skipDialogues()
    }
}
