package content.quest.member.ghosts_ahoy

import WorldTest
import dialogueOption
import itemOnObject
import itemOption
import messages
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class GhostsAhoyTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(3677, 3509))
        player.inventory.add("bowl")
        player.equipment.set(EquipSlot.Amulet.index, "ghostspeak_amulet")
        player.equipment.set(EquipSlot.Hands.index, "leather_gloves")
        player["priest_in_peril"] = "completed"
        player["the_restless_ghost"] = "completed"

        val velorina = NPCs.findBySpawn(Tile(3678, 3510), "ahoy_velorina")

        player.npcOption(velorina, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1) // What's the matter?
        player.skipDialogues()
        player.dialogueOption(1) // Very sad
        player.skipDialogues()
        player.dialogueOption(1) // Yes
        player.skipDialogues()
        assertEquals(1, player["ghosts_ahoy", 0])

        player.tele(3660, 3517)
        val necrovarus = NPCs.findBySpawn(Tile(3660, 3516), "ahoy_necrovarus")
        player.npcOption(necrovarus, "Talk-to")
        tick(1)
        player.skipDialogues()
        assertEquals(2, player["ghosts_ahoy", 0])

        player.tele(3677, 3509)
        player.npcOption(velorina, "Talk-to")
        tick(1)
        player.skipDialogues()
        assertEquals(3, player["ghosts_ahoy", 0])

        // Fill bowl
        player.tele(3588, 3528)
        val waterpump = GameObjects.find(Tile(3588, 3529), "waterpump_morytania")
        player.itemOnObject(waterpump, player.inventory.indexOf("bowl"))
        tick(1)
        assertEquals(0, player.inventory.count("bowl"))
        assertEquals(1, player.inventory.count("bowl_of_water"))

        // Pick nettles
        player.tele(3588, 3528)
        val nettles = GameObjects.find(Tile(3525, 3512), "nettles_6")
        player.objectOption(nettles, "Pick")
        tick(1)
        assertEquals(1, player.inventory.count("nettles"))

        player.tele(3461, 3557)
        val oldCrone = NPCs.findBySpawn(Tile(3461, 3558), "ahoy_crone")
        player.npcOption(oldCrone, "Talk-to")
        tick(1)
        player.skipDialogues()
        assertEquals(1, player["ahoy_subquest_nettletea", 0])


    }
}