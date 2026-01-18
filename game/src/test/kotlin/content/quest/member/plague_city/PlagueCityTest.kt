package content.quest.member.plague_city

import WorldTest
import dialogueContinue
import dialogueOption
import equipItem
import interfaceOption
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlagueCityTest : WorldTest() {

    @Test
    fun `Complete plague city`() {
        val player = createPlayer(Tile(2568, 3333))
        player.inventory.add("dwellberries")
        player.inventory.add("bucket_of_water", 4)
        player.inventory.add("spade")
        player.inventory.add("rope")
        player.inventory.add("picture_plague_city")
        player.inventory.add("hangover_cure")
        val edmond = createNPC("edmond_ardougne", Tile(2568, 3334))
        player.npcOption(edmond, "Talk-to")
        tick()
        player.dialogueContinue(4)
        player.dialogueOption("line1")
        player.dialogueContinue(7)
        assertEquals("quest_intro", player.menu)
        player.interfaceOption("quest_intro", "startyes_layer", "Yes")
        player.dialogueContinue(7)
        assertNull(player.dialogue)

        player.tele(2572, 3333)

        val alrena = createNPC("alrena", Tile(2573, 3333))
        player.npcOption(alrena, "Talk-to")
        tick()
        player.dialogueContinue(8)
        assertEquals(0, player.inventory.count("dwellberries"))
        assertEquals(1, player.inventory.count("gas_mask"))
        assertNull(player.dialogue)

        player.tele(2568, 3333)
        player.npcOption(edmond, "Talk-to")
        tick()
        player.dialogueContinue(3)

        player.tele(2567, 3332)
        val soil = GameObjects.find(Tile(2566, 3332), "plague_mud_patch2")
        repeat(4) {
            player.itemOnObject(soil, player.inventory.indexOf("bucket_of_water"))
            tick()
            player.dialogueContinue()
        }
        assertEquals(0, player.inventory.count("bucket_of_water"))
        assertEquals(4, player.inventory.count("bucket"))
        player.itemOnObject(soil, player.inventory.indexOf("spade"))
        tick(6)
        player.dialogueContinue()
        assertEquals(Tile(2518, 9760), player.tile)

        player.tele(2514, 9739)
        val grill = GameObjects.find(Tile(2514, 9739), "plague_grill")
        player.itemOnObject(grill, player.inventory.indexOf("rope"))
        tick(3)
        player.dialogueContinue()
        assertNull(player.dialogue)

        player.tele(2517, 9754)
        val edmondSewer = createNPC("edmond_sewer", Tile(2517, 9755))
        player.npcOption(edmondSewer, "Talk-to")
        tick()
        player.dialogueContinue(2)
        tick(30)
        assertEquals(Tile(2514, 9740), player.tile)

        player.equipItem("gas_mask", option = "Wear")
        tick(2)
        assertEquals("gas_mask", player.equipped(EquipSlot.Hat).id)

        player.tele(2514, 9739)
        val pipe = GameObjects.find(Tile(2514, 9737), "plague_sewer_pipe_open")
        player.objectOption(pipe, "Climb-up")
        tick(4)
        assertEquals(Tile(2529, 3304), player.tile)

        player.tele(2539, 3305)
        val jethick = createNPC("jethick", Tile(2540, 3305))
        player.npcOption(jethick, "Talk-to")
        tick()
        player.dialogueContinue(17)
        player.dialogueOption("line1")
        player.dialogueContinue(2)
        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("book_turnip_growing_for_beginners"))

        player.tele(2531, 3328)
        val door = GameObjects.find(Tile(2531, 3328), "door_rehnison_closed")
        player.objectOption(door, "Open")
        tick()
        player.dialogueContinue(3)
        tick(2)
        player.dialogueContinue(2)
        assertEquals(Tile(2531, 3329), player.tile)
        assertEquals(0, player.inventory.count("book_turnip_growing_for_beginners"))

        player.tele(2531, 3330)
        val ted = createNPC("ted_rehnison", Tile(2531, 3331))
        player.npcOption(ted, "Talk-to")
        tick()
        player.dialogueContinue(3)
        assertNull(player.dialogue)

        player.tele(2531, 3331, 1)
        val milli = createNPC("milli_rehnison", Tile(2531, 3330, 1))
        player.npcOption(milli, "Talk-to")
        tick()
        player.dialogueContinue(4)
        assertNull(player.dialogue)

        player.tele(2533, 3273, 0)
        val plagueDoor = GameObjects.find(Tile(2533, 3273), "door_plague_city_closed")
        player.objectOption(plagueDoor, "Open")
        tick()
        player.dialogueContinue(2)
        player.dialogueOption("line1")
        player.dialogueContinue(2)
        player.dialogueOption("line2")
        player.dialogueContinue(5)
        assertNull(player.dialogue)

        player.tele(2527, 3319)
        val clerk = createNPC("clerk_west_ardougne", Tile(2526, 3319))
        player.npcOption(clerk, "Talk-to")
        tick(2)
        player.dialogueContinue()
        player.dialogueOption("line2")
        player.dialogueContinue(4)
        player.dialogueOption("line1")
        player.dialogueContinue(4)

        player.tele(2532, 3315, 1)
        val bravekDoor = GameObjects.find(Tile(2533, 3315, 1), "door_civic_office_closed")
        player.objectOption(bravekDoor, "Open")
        tick(3)
        assertEquals(Tile(2533, 3315, 1), player.tile)

        player.tele(2535, 3314, 1)
        val bravek = createNPC("bravek", Tile(2536, 3314, 1))
        player.npcOption(bravek, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption("line1")
        player.dialogueContinue(2)
        player.dialogueOption("line3")
        player.dialogueContinue(3)
        assertTrue(player.inventory.contains("a_scruffy_note"))

        player.npcOption(bravek, "Talk-to")
        tick()
        player.dialogueContinue(6)
        player.dialogueOption("line3")
        player.dialogueContinue(5)
        assertTrue(player.inventory.contains("warrant"))

        player.tele(2533, 3273, 0)
        val guard = createNPC("mourner_elena_guard_vis", Tile(2534, 3273))
        createNPC("mourner_elena_guard_vis", Tile(2539, 3273))
        player.npcOption(guard, "Talk-to")
        tick()
        player.dialogueContinue(2)
        tick(10)
        assertEquals(Tile(2533, 3272), player.tile)

        player.tele(2535, 3268, 0)
        val barrel = GameObjects.find(Tile(2534, 3268), "plague_key_barrel")
        player.objectOption(barrel, "Search")
        tick()
        player.dialogueContinue()
        assertEquals(1, player.inventory.count("a_small_key"))

        player.tele(2539, 9672, 0)
        val prisonDoor = GameObjects.find(Tile(2539, 9672), "door_elena_prison_closed")
        player.objectOption(prisonDoor, "Open")
        tick(3)
        assertEquals(Tile(2540, 9672), player.tile)

        player.tele(2540, 9672)
        val elena = createNPC("elenap_vis", Tile(2541, 9672))
        player.npcOption(elena, "Talk-to")
        tick()
        player.dialogueContinue(4)
        tick(8)
        assertEquals("freed_elena", player["plague_city", ""])
        assertNull(player.dialogue)

        player.tele(2568, 3333)
        player.npcOption(edmond, "Talk-to")
        tick()
        player.dialogueContinue(1)

        assertEquals(2425.0, player.experience.get(Skill.Mining))
        assertEquals(1, player.inventory.count("a_magic_scroll"))
        assertEquals(1, player["quest_points", 0])
    }
}
