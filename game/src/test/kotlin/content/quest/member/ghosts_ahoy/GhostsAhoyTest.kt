package content.quest.member.ghosts_ahoy

import FakeRandom
import WorldTest
import dialogueOption
import itemOnItem
import itemOnObject
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class GhostsAhoyTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(3677, 3509))
        player.levels.set(Skill.Firemaking, 99)
        player.levels.set(Skill.Cooking, 20)
        player["insta_kill"] = true
        player["auto_retaliate"] = true
        player.inventory.add("bowl")
        player.inventory.add("logs")
        player.inventory.add("tinderbox")
        player.inventory.add("bucket_of_milk")
        player.inventory.add("needle")
        player.inventory.add("thread")
        player.inventory.add("knife")
        player.inventory.add("silk")
        player.inventory.add("spade")
        player.inventory.add("oak_longbow")
        player.inventory.add("coins", 400)
        player.inventory.add("ecto_token", 25)
        player.inventory.add("bucket_of_slime")
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
        player.itemOnItem("nettles", "bowl_of_water")
        tick(4)
        assertEquals(1, player.inventory.count("nettle_water"))
        player.itemOnItem("tinderbox", "logs")
        tick(5)
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

        player.itemOnItem("nettle_tea", "porcelain_cup")
        tick(2)
        assertEquals(1, player.inventory.count("cup_of_tea_ghosts_ahoy"))
        player.itemOnItem("bucket_of_milk", "cup_of_tea_ghosts_ahoy")
        tick(2)
        assertEquals(1, player.inventory.count("cup_of_milky_tea_ghosts_ahoy"))
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

        // Dye boat
        var dye = "${GhostsAhoy.flagColor(player["ahoy_mast_top", 0])}_dye"
        player.inventory.add(dye)
        player.itemOnItem(dye, "model_ship_silk")
        tick(1)
        player.dialogueOption(1)
        tick(1)
        assertNotEquals(0, player["ahoy_toy_top", 0])

        dye = "${GhostsAhoy.flagColor(player["ahoy_mast_bottom", 0])}_dye"
        player.inventory.add(dye)
        player.itemOnItem(dye, "model_ship_silk")
        tick(1)
        player.dialogueOption(2)
        tick(1)
        assertNotEquals(0, player["ahoy_toy_bottom", 0])

        dye = "${GhostsAhoy.flagColor(player["ahoy_mast_skull", 0])}_dye"
        player.inventory.add(dye)
        player.itemOnItem(dye, "model_ship_silk")
        tick(1)
        player.dialogueOption(3)
        tick(1)
        assertNotEquals(0, player["ahoy_toy_skull", 0])

        // Get key
        player.tele(3616, 3544, 1)
        val oldMan = NPCs.findBySpawn(Tile(3617, 3544, 1), "ahoy_oldman")
        player.npcOption(oldMan, "Talk-To")
        tick(1)
        player.dialogueOption(3)
        player.skipDialogues()
        assertEquals(1, player.inventory.count("chest_key_ghosts_ahoy"))

        // First map piece
        player.tele(3619, 3544, 1)
        var chest = GameObjects.find(Tile(3619, 3545, 1), "ahoy_pirate_chest_locked")
        player.itemOnObject(chest, player.inventory.indexOf("chest_key_ghosts_ahoy"))
        tick(1)
        player.objectOption(chest, "Open")
        tick(1)
        assertEquals(0, player.inventory.count("chest_key_ghosts_ahoy"))
        assertEquals(1, player.inventory.count("map_scrap_1"))

        // Second map piece
        player.tele(3618, 3543, 0)
        chest = GameObjects.find(Tile(3618, 3542), "ahoy_pirate_chest_closed")
        player.objectOption(chest, "Open")
        tick(3)
        chest = GameObjects.find(Tile(3618, 3542), "ahoy_pirate_chest_open")
        player.objectOption(chest, "Search")
        tick(4) // Kill giant lobster
        player.objectOption(chest, "Search")
        tick(2)
        assertEquals(1, player.inventory.count("map_scrap_2"))

        // Third map piece
        player.tele(3605, 3564)
        chest = GameObjects.find(Tile(3606, 3564), "ahoy_pirate_chest_closed")
        player.objectOption(chest, "Open")
        tick(3)
        chest = GameObjects.find(Tile(3606, 3564), "ahoy_pirate_chest_open")
        player.objectOption(chest, "Search")
        tick(2)
        assertEquals(1, player.inventory.count("map_scrap_3"))

        // Make treasure map
        player.itemOnItem("map_scrap_2", "map_scrap_3")
        tick(1)
        assertEquals(0, player.inventory.count("map_scrap_1"))
        assertEquals(0, player.inventory.count("map_scrap_2"))
        assertEquals(0, player.inventory.count("map_scrap_3"))
        assertEquals(1, player.inventory.count("treasure_map"))

        // Board boat
        player.tele(3702, 3487)
        val captain = NPCs.findBySpawn(Tile(3703, 3487), "ahoy_ghost_captain_1")
        player.npcOption(captain, "Talk-To")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        tick(10)
        assertEquals(Tile(3792, 3559), player.tile)

        // Find Book of Haricanto
        player.tele(3803, 3530)
        player.itemOption("Dig", "spade")
        tick(3)
        assertEquals(1, player.inventory.count("book_of_haricanto"))

        // Ak-Haranu
        player.tele(3689, 3496)
        val akharanu = NPCs.findBySpawn(Tile(3689, 3495), "ahoy_akharanu_multi")
        player.npcOption(akharanu, "Talk-To")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        assertEquals(1, player["ahoy_subquest_bow", 0])

        // Robin
        player.tele(3672, 3492)
        player["ahoy_subquest_bow"] = 7 // Skip minigame
        val robin = NPCs.findBySpawn(Tile(3672, 3491), "ahoy_robin")
        player.npcOption(robin, "Talk-To")
        tick(1)
        player.skipDialogues()
        assertEquals(1, player.inventory.count("signed_oak_bow"))

        // Get manual
        player.tele(3689, 3496)
        player.npcOption(akharanu, "Talk-To")
        tick(1)
        player.skipDialogues()
        assertEquals(1, player.inventory.count("translation_manual"))

        // Innkeeper
        player.tele(3681, 3495)
        val innkeeper = NPCs.findBySpawn(Tile(3681, 3496), "ahoy_ghost_innkeeper")
        player.npcOption(innkeeper, "Talk-To")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(3) // Any jobs
        player.skipDialogues()
        player.dialogueOption(1) // Yes
        player.skipDialogues()
        assertEquals(1, player.inventory.count("bedsheet"))

        // Bedsheet
        player.itemOnItem("bucket_of_slime", "bedsheet")
        tick(3)
        assertEquals(0, player.inventory.count("bedsheet"))
        assertEquals(1, player.inventory.count("bedsheet_ectoplasm"))

        // Protest
        player.tele(3660, 3500)
        val grava = NPCs.findBySpawn(Tile(3660, 3499), "protester_standardspeak_multi")
        player.npcOption(grava, "Talk-To")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        assertEquals(1, player.inventory.count("petition_form"))

        // Petition
        player.equipment.set(EquipSlot.Hat.index, "bedsheet_ectoplasm")
        player.tele(3661, 3496)
        player.inventory.add("ecto_token", 5)
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int) = until - 1
        })
        val villager = NPCs.findBySpawn(Tile(3661, 3497), "ahoy_ghost_villager")
        player.npcOption(villager, "Talk-To")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        assertEquals(2, player["ahoy_signaturecounter", 0])

        player["ahoy_signaturecounter"] = 11
        player.tele(3660, 3500)
        player.npcOption(grava, "Talk-To")
        tick(1)
        player.skipDialogues()

        // Necrovarus
        player.tele(3660, 3517)
        player.npcOption(necrovarus, "Talk-To")
        tick(1)
        player.skipDialogues()
        tick(2)
        assertEquals(31, player["ahoy_signaturecounter", 0])

        assertNotNull(FloorItems.firstOrNull(necrovarus.tile, "bone_key_ghosts_ahoy"))
        player.inventory.add("bone_key_ghosts_ahoy")

        // Open door
        player.tele(3655, 3514, 1)
        val door = GameObjects.find(Tile(3656, 3514, 1), "ahoy_harbour_door_closed")
        player.itemOnObject(door, player.inventory.indexOf("bone_key_ghosts_ahoy"))
        tick(4)
        assertTrue(player["ahoy_templedoor_unlocked", false])

        player.objectOption(door, "Open")
        tick(5)
        assertEquals(Tile(3656, 3514, 1), player.tile)

        // Open coffin
        player.tele(3658, 3514, 1)
        var coffin = GameObjects.find(Tile(3659, 3513, 1), "ahoy_necrovarus_coffin_closed")
        player.objectOption(coffin, "Open")
        tick(1)
        coffin = GameObjects.find(Tile(3659, 3513, 1), "ahoy_necrovarus_coffin_open")
        player.objectOption(coffin, "Search")
        tick(1)
        player.skipDialogues()
        assertEquals(1, player.inventory.count("mystical_robes"))

        // Return all items to old crone
        player.tele(3461, 3557, 0)
        player.npcOption(oldCrone, "Talk-To")
        tick(1)
        player.skipDialogues()
        tick(5)
        assertEquals("ghostspeak_amulet_enchanted", player.equipped(EquipSlot.Amulet).id)
        assertEquals(6, player["ahoy_questvar", 0])

        // Command Necrovarus
        player.tele(3660, 3517)
        player.npcOption(necrovarus, "Talk-To")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        assertEquals(7, player["ahoy_questvar", 0])

        // Return to vel
        player.tele(3677, 3509)
        player.npcOption(velorina, "Talk-To")
        tick(1)
        player.skipDialogues()
        assertEquals(1, player.inventory.count("ectophial"))
        assertEquals(8, player["ahoy_questvar", 0])
    }
}
