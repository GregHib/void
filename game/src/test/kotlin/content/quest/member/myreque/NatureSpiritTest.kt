package content.quest.member.myreque

import WorldTest
import content.quest.quest
import continueDialogue
import dialogueOption
import floorItemOption
import interfaceOption
import itemOnNpc
import itemOption
import messages
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NatureSpiritTest : WorldTest() {

    override var loadNpcs: Boolean = true

    @Test
    fun `Complete quest`() {
        val player = createPlayer(Tile(3439, 9895))
        player.equipment.set(EquipSlot.Amulet.index, "ghostspeak_amulet")
        player["priest_in_peril"] = "completed_wolfbane"

        val drezel = NPCs.findBySpawn(Tile(3440, 9895), "priestperiltrappedmonk2")
        player.npcOption(drezel, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(2) // Anything else interesting
        player.skipDialogues()
        player.dialogueOption(2) // What is it?
        player.skipDialogues()
        player.dialogueOption(4) // Yes, I'll go look
        player.skipDialogues()
        player.dialogueOption(1) // Yes, I'm sure
        player.skipDialogues()
        assertEquals("find_filliman", player.quest("nature_spirit"))

        player.tele(3444, 3458)
        val swampGate = GameObjects.find(Tile(3444, 3458), "gate_mort_myre_closed")
        player.objectOption(swampGate, "Open")
        tick(1)
        player.interfaceOption("warning_mort_myre", "yes", "Yes")
        tick(4)
        assertEquals(Tile(3444, 3457), player.tile)

        player.tele(3440, 3337)
        println(player.inventory.items.toList())
        val grotto = GameObjects.find(Tile(3440, 3337), "grotto_door_druidicspirit")
        player.objectOption(grotto, "Enter")
        tick(2)
        player.skipDialogues()

        // Convince filliman he's a ghost
        val bowl = FloorItems.add(Tile(3437, 3337), "washing_bowl")
        player.floorItemOption(bowl, "Take")
        tick(4)
        assertTrue(player.inventory.contains("washing_bowl"))
        val mirror = FloorItems.first(Tile(3437, 3337), "mirror")
        player.floorItemOption(mirror, "Take")
        tick(3)
        assertTrue(player.inventory.contains("mirror"))

        val filliman = NPCs.find(Tile(3440, 3336), "filliman_tarlock_ghost")
        player.itemOnNpc(filliman, player.inventory.indexOf("mirror"))
        tick(1)
        player.skipDialogues()

        val grottoTree = GameObjects.find(Tile(3439, 3338), "grotto_druidicspirit")
        player.objectOption(grottoTree, "Search")
        tick(6)
        player.skipDialogues()
        assertTrue(player.inventory.contains("journal_nature_spirit"))

        player.itemOnNpc(filliman, player.inventory.indexOf("journal_nature_spirit"))
        tick(4)
        player.skipDialogues()
        player.dialogueOption(4)
        player.skipDialogues()
        assertEquals("spell", player.quest("nature_spirit"))
        assertTrue(player.inventory.contains("druidic_spell"))

        // Return to drezel
        player.tele(3439, 9895)
        player.npcOption(drezel, "Talk-to")
        tick(1)
        player.skipDialogues()
        tick(3)
        player.skipDialogues()
        assertEquals("blessed_spell", player.quest("nature_spirit"))

        // Cast spell
        player.tele(3440, 3455)
        player.itemOption("Cast", "druidic_spell")
        tick(3)

        val log = GameObjects.find(Tile(3440, 3454), "log_druidicspirit") // TODO transform
        player.objectOption(log, "Pick")
        tick(2)
        assertTrue(player.inventory.contains("mort_myre_fungus"))
    }


}