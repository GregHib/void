package content.quest.member.zogre_flesh_eaters

import WorldTest
import dialogueOption
import itemOnFloorItem
import itemOnNpc
import itemOnObject
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import skipDialogues
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZogreFleshEatersTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the test`() {
        val player = createPlayer(Tile(2445, 3052))
        player.levels.set(Skill.Ranged, 30)
        player.experience.set(Skill.Ranged, Level.experience(30))
        player["chompy_birds"] = 65
        player["jungle_potion"] = "completed"
        val grish = NPCs.findBySpawn(Tile(2443, 3051), "grish")

        player.npcOption(grish, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(2) // Sickies?
        player.skipDialogues()
        player.dialogueOption(4) // Can I help?
        player.skipDialogues()
        player.dialogueOption(2) // Ok
        player.skipDialogues()
        player.dialogueOption(1) // Really sure
        player.skipDialogues()
        tick(4)

        assertEquals(2, player["zogre_flesh_eaters", 0])
        assertEquals(3, player.inventory.count("cooked_chompy"))
        assertEquals(2, player.inventory.count("super_restore_3"))

        val guard = NPCs.findBySpawn(Tile(2454, 3047), "zogre_ogre_guard")
        player.tele(2453, 3048)
        player.npcOption(guard, "Talk-to")
        tick(1)
        player.skipDialogues()
        tick(6)
        assertEquals(3, player["zogre_flesh_eaters", 0])

        val barricade = GameObjects.find(Tile(2456, 3049), "ogre_barricade_collapsed")
        player.objectOption(barricade, "Climb-over")
        tick(6)
        assertEquals(Tile(2457, 3049), player.tile)

        player.tele(2443, 9460, 2)
        val lecturn = GameObjects.find(Tile(2443, 9459, 2), "zogre_lecturn")
        player.objectOption(lecturn, "Search")
        tick(3)
        assertTrue(player.inventory.contains("torn_page"))

        player.tele(2442, 9459, 2)
        player["insta_kill"] = true
        player["auto_retaliate"] = true
        val skeleton = GameObjects.find(Tile(2442, 9459, 2), "zogre_brentle_skeleton")
        player.objectOption(skeleton, "Search")
        tick(15)

        assertTrue(FloorItems.at(player.tile.zone).any { list -> list.any { item -> item.id == "ruined_backpack" }})
        player.inventory.add("ruined_backpack")


        player.itemOption("Open", "ruined_backpack")
        tick(1)
        player.skipDialogues()
        assertTrue(player.inventory.contains("dragon_inn_tankard"))
        assertTrue(player.inventory.contains("rotten_food"))
        assertTrue(player.inventory.contains("knife"))

        player.tele(2440, 9459, 2)
        val coffin = GameObjects.find(Tile(2438, 9458, 2), "zogre_coffin_base")
        player.interactObject(coffin, "Search")
        tick(1)
        player.skipDialogues()
        player.itemOnObject(coffin, player.inventory.indexOf("knife"))
        tick(4)
        player.interactObject(coffin, "Search")
        tick(1)
        player.dialogueOption("continue")
        setRandom(object : Random() {
            override fun nextBits(bitCount: Int) = 0
        })
        tick(8)
        player.skipDialogues()
        tick(2)
        player.interactObject(coffin, "Search")
        tick(4)
        assertTrue(player.inventory.contains("black_prism"))
        assertEquals(3, player["thzfe_prismsearch", 0])

        player.tele(2556, 3079, 0)
        val bartender = NPCs.findBySpawn(Tile(2556, 3078), "bartender_dragon_inn")
        player.itemOnNpc(bartender, player.inventory.indexOf("dragon_inn_tankard"))
        tick(1)
        player.skipDialogues()
        assertTrue(player["thzfe_showntankard", false])

        player.tele(2598, 3086)
        val bell = GameObjects.find(Tile(2598, 3085), "zogre_outdoor_bell")
        player.objectOption(bell, "Ring")
        tick(1)
        player.skipDialogues()
        assertEquals(4, player["thzfe_prismsearch", 0])

        player.tele(2590, 3104, 1)
        val sithik = GameObjects.find(Tile(2591, 3103, 1), "zogre_sithik_bed")
        player.objectOption(sithik, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        player.objectOption(sithik, "Talk-to")
        tick(1)
        player.dialogueOption(2)
        player.skipDialogues()
        assertEquals(5, player["thzfe_prismsearch", 0])

        player.tele(2590, 3104, 1)
        val wardrobe = GameObjects.find(Tile(2590, 3103, 1), "sithiks_wardrobe")
        player.objectOption(wardrobe, "Search")
        tick(1)
        assertTrue(player.inventory.contains("book_of_ham"))

        player.tele(2593, 3105, 1)
        val cupboard = GameObjects.find(Tile(2594, 3104, 1), "sithiks_cupboard")
        player.objectOption(cupboard, "Search")
        tick(1)
        assertTrue(player.inventory.contains("necromancy_book"))

        player.tele(2593, 3103, 1)
        val drawers = GameObjects.find(Tile(2594, 3103, 1), "sithiks_drawers")
        player.objectOption(drawers, "Search")
        tick(1)
        player.skipDialogues()
        assertTrue(player.inventory.contains("charcoal"))
        assertTrue(player.inventory.contains("papyrus"))
        assertTrue(player.inventory.contains("book_of_portraiture"))

        player.itemOnObject(sithik, player.inventory.indexOf("papyrus"))
        tick(1)
        player.skipDialogues()
        tick(2)
        assertTrue(player.inventory.contains("zogre_sithik_portrait_good"))

        player.tele(2556, 3079, 0)
        player.itemOnNpc(bartender, player.inventory.indexOf("zogre_sithik_portrait_good"))
        tick(1)
        player.skipDialogues()
        assertTrue(player["thzfe_innkeeperportraitshown", false])


        player.tele(2588, 3090, 1)
        val rarve = NPCs.findBySpawn(Tile(2588, 3091, 1), "zavistic_rarve")
        player.npcOption(rarve, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(3)
        player.skipDialogues()
        assertEquals(4, player["zogre_flesh_eaters", 0])
        assertTrue(player.inventory.contains("zogre_ogre_trans_potion"))
        assertFalse(player.inventory.contains("book_of_ham"))
        assertFalse(player.inventory.contains("necromancy_book"))
        assertFalse(player.inventory.contains("zogre_sithik_portrait_signed"))

        player.tele(2593, 3103, 1)
        val floorItem = FloorItems.add(Tile(2594, 3103, 1), "cup_of_tea_zogre_flesh_eaters")
        player.itemOnFloorItem(floorItem, player.inventory.indexOf("zogre_ogre_trans_potion"))
        tick(3)
        player.skipDialogues()
        assertEquals(6, player["zogre_flesh_eaters", 0])

        player.tele(2597, 3108, 0)
        val ladder = GameObjects.find(Tile(2597, 3107), "basic_ladder_bottom")
        player.objectOption(ladder, "Climb-up")
        tick(2)
        assertEquals(1, player["thzfe_sithik_transformed", 0])

        player.tele(2593, 3103, 1)
        player.objectOption(sithik, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        assertEquals(8, player["zogre_flesh_eaters"])
        player.dialogueOption(2)
        player.skipDialogues()
        assertTrue(player["thzfe_makebrutalarrow", false])
        player.dialogueOption(3)
        player.skipDialogues()
        assertTrue(player["thzfe_makecuredisease", false])
        assertEquals(8, player["zogre_flesh_eaters", 0])

        player.tele(2445, 3052, 0)
        player.npcOption(grish, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        assertTrue(player.inventory.contains("ogre_gate_key"))

        player.tele(2482, 9445)
        val stand = GameObjects.find(Tile(2483, 9445), "zogre_stand")
        player.objectOption(stand, "Search")
        tick(1)
        player.skipDialogues()
        tick(15)

        assertEquals(12, player["zogre_flesh_eaters", 0])
        val artifact = FloorItems.firstOrNull(Tile(2477, 9444), "ogre_artefact")
        assertNotNull(artifact)

        player.objectOption(stand, "Search")
        tick(4)
        player.skipDialogues()
        assertTrue(player.inventory.contains("ogre_artefact"))

        player.tele(2445, 3052, 0)
        player.npcOption(grish, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        tick(1)
        assertEquals(14, player["zogre_flesh_eaters", 0])
    }
}