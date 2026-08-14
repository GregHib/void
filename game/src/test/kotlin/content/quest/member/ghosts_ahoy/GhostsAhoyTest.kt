package content.quest.member.ghosts_ahoy

import WorldTest
import dialogueOption
import itemOnItem
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
import world.gregs.voidps.engine.entity.character.player.skill.Skill
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
        player.levels.set(Skill.Firemaking, 99)
        player.levels.set(Skill.Cooking, 20)
        player["debug"] = true
        player.inventory.add("bowl")
        player.inventory.add("logs")
        player.inventory.add("tinderbox")
        player.inventory.add("bucket_of_milk")
        player.inventory.add("needle")
        player.inventory.add("thread")
        player.inventory.add("knife")
        player.inventory.add("silk")
        player.equipment.set(EquipSlot.Amulet.index, "ghostspeak_amulet")
        player.equipment.set(EquipSlot.Hands.index, "leather_gloves")
        player["priest_in_peril"] = "completed"
        player["the_restless_ghost"] = "completed"

        // Talk with velorina
        val velorina = NPCs.findBySpawn(Tile(3678, 3510), "ahoy_velorina")
        player.npcOption(velorina, "Talk-To")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1) // What's the matter?
        player.skipDialogues()
        player.dialogueOption(1) // Very sad
        player.skipDialogues()
        player.dialogueOption(1) // Yes
        player.skipDialogues()
        assertEquals(1, player["ahoy_questvar", 0])

        // Talk with necrovarus
        player.tele(3660, 3517)
        val necrovarus = NPCs.findBySpawn(Tile(3660, 3516), "ahoy_necrovarus")
        player.npcOption(necrovarus, "Talk-To")
        tick(1)
        player.skipDialogues()
        assertEquals(2, player["ahoy_questvar", 0])

        // Return to velorina
        player.tele(3677, 3509)
        player.npcOption(velorina, "Talk-To")
        tick(1)
        player.skipDialogues()
        assertEquals(3, player["ahoy_questvar", 0])

        // Fill bowl
        player.tele(3588, 3528)
        val waterpump = GameObjects.find(Tile(3588, 3529), "waterpump_morytania")
        player.itemOnObject(waterpump, player.inventory.indexOf("bowl"))
        tick(1)
        assertEquals(0, player.inventory.count("bowl"))
        assertEquals(1, player.inventory.count("bowl_of_water"))

        // Pick nettles
        player.tele(3524, 3512)
        tick(1)
        val nettles = GameObjects.find(Tile(3525, 3512), "nettles_6")
        player.objectOption(nettles, "Pick")
        tick(2)
        assertEquals(1, player.inventory.count("nettles"))

        // Make nettle water
        player.tele(3523, 3512)
        player.itemOnItem(player.inventory.indexOf("nettles"), player.inventory.indexOf("bowl_of_water"))
        tick(4)
        println(player.messages)
        assertEquals(1, player.inventory.count("nettle_water"))
        player.itemOnItem(player.inventory.indexOf("tinderbox"), player.inventory.indexOf("logs"))
        tick(5)
        println(GameObjects.at(Tile(3523, 3512)))
        val fire = GameObjects.find(Tile(3523, 3512), "fire_orange")
        player.itemOnObject(fire, player.inventory.indexOf("nettle_water"))
        tick(5)
        assertEquals(0, player.inventory.count("nettle_water"))
        assertEquals(1, player.inventory.count("nettle_tea"))

        // Old crone
        player.tele(3461, 3557)
        val oldCrone = NPCs.findBySpawn(Tile(3461, 3558), "ahoy_crone")
        player.npcOption(oldCrone, "Talk-To")
        tick(1)
        player.skipDialogues()
        assertEquals(2, player["ahoy_subquest_nettletea", 0])

        player.itemOnItem(player.inventory.indexOf("nettle_tea"), player.inventory.indexOf("porcelain_cup"))
        tick(1)
        player.itemOnItem(player.inventory.indexOf("bucket_of_milk"), player.inventory.indexOf("cup_of_tea_ghosts_ahoy"))
        tick(1)
        player.npcOption(oldCrone, "Talk-To")
        tick(1)
        player.skipDialogues()
        assertEquals(3, player["ahoy_subquest_nettletea", 0])

        player.dialogueOption(1) // Son
        player.skipDialogues()
        assertEquals(1, player["ahoy_subquest_toyboat", 0])

        // Repair boat
        player.itemOption("Repair", "model_ship")
        tick(2)
        assertEquals(0, player.inventory.count("model_ship"))
        assertEquals(1, player.inventory.count("model_ship_silk"))


    }
}