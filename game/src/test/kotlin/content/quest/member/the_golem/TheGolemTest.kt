package content.quest.member.the_golem

import WorldTest
import containsMessage
import dialogueContinue
import dialogueOption
import itemOnItem
import itemOnNpc
import itemOnObject
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TheGolemTest : WorldTest() {

    @Test
    fun `Complete the golem quest`() {
        val player = createPlayer(Tile(3489, 3090))
        player.experience.set(Skill.Crafting, Level.experience(20))
        player.experience.set(Skill.Thieving, Level.experience(25))

        // Stage unstarted -> repair_golem: the broken golem asks to be mended
        val golem = createNPC("clay_golem_ruins_of_uzer", Tile(3488, 3090))
        player.npcOption(golem, "Talk-to")
        tick(2)
        player.dialogueContinue() // npc "Damage... severe...<br>task... incomplete..."
        player.dialogueOption("line1") // Can I repair you?
        player.dialogueContinue(2)
        assertEquals("repair_golem", player["the_golem", "unstarted"])

        // Stage repair_golem -> open_portal: four pieces of soft clay
        player.inventory.add("soft_clay", 4)
        repeat(4) {
            player.itemOnNpc(golem, player.inventory.indexOf("soft_clay"))
            tick(2)
            player.dialogueContinue()
        }
        assertEquals(0, player.inventory.count("soft_clay"))
        assertEquals(4, player["golem_clay", 0])
        player.dialogueContinue(7) // repairComplete's chathead lines
        assertEquals("open_portal", player["the_golem", "unstarted"])

        // Explore the temple below Uzer; the curator only discusses the statuette
        // once golem_seen_underground is set.
        player.tele(3491, 3090)
        val stairsDown = GameObjects.find(Tile(3492, 3089), "sote_warped_library_floor_lvl2_centre_01")
        player.objectOption(stairsDown, "Climb-down")
        tick(3)
        assertTrue(player["golem_seen_underground", false])
        assertEquals(Tile(2721, 4886), player.tile)

        val stairsUp = GameObjects.find(Tile(2721, 4884), "golem_insidestairs_base")
        player.objectOption(stairsUp, "Climb-up")
        tick(3)
        assertEquals(Tile(3491, 3090), player.tile)

        // Varmen's letter points at Elissa
        player.inventory.add("letter_the_golem")
        player.itemOption("Read", "letter_the_golem")
        tick(2)
        assertEquals(1, player["golem_b", 0])

        // Elissa points at the Exam Centre bookcase
        player.tele(3380, 3443)
        val elissa = createNPC("elissa_digsite", Tile(3380, 3442))
        player.npcOption(elissa, "Talk-to")
        tick(2)
        player.dialogueContinue() // npc "Hello there."
        player.dialogueOption("line3") // I found a letter in the desert...
        player.dialogueContinue(5)
        assertEquals(2, player["golem_b", 0])

        // Take Varmen's notes from the Exam Centre bookcase
        player.tele(3366, 3333)
        val bookcase = GameObjects.find(Tile(3366, 3332), "qip_digsite_bookcase_low_digbookcase_shorter_m_o")
        player.objectOption(bookcase, "Search")
        tick(6)
        player.dialogueContinue()
        assertTrue(player.inventory.contains("varmens_notes"))
        assertEquals(3, player["golem_b", 0])

        // Stage open_portal -> find_statuette: ask the curator about the statuette,
        // then pickpocket him for the display cabinet key.
        player.tele(3257, 3447)
        val curator = createNPC("curator_haig_halen", Tile(3257, 3446))
        player.npcOption(curator, "Talk-to")
        tick(2)
        player.dialogueContinue() // npc "Welcome to the museum of Varrock."
        player.dialogueOption("line3") // I'm looking for a statuette...
        player.dialogueContinue(5)
        assertEquals("find_statuette", player["the_golem", "unstarted"])
        player.dialogueOption("line2") // Well, I, er, just want it.
        player.dialogueContinue(2)

        player.npcOption(curator, "Pickpocket")
        tick(2)
        player.dialogueContinue()
        assertTrue(player.inventory.contains("display_cabinet_key"))

        // Take the statuette from the Varrock Museum display case
        player.tele(3255, 3454, 1)
        val displayCase = GameObjects.find(Tile(3255, 3453, 1), "vm_timeline_terracotta_statue")
        player.objectOption(displayCase, "Open")
        tick(2)
        player.dialogueContinue()
        assertTrue(player.inventory.contains("statuette_the_golem"))
        assertTrue(player["golem_retrieved_statuette", false])

        // Stage find_statuette -> statuette_replaced: put it back in the empty alcove
        player.tele(2724, 4896, 0)
        val emptyAlcove = GameObjects.find(Tile(2725, 4896), "golem_statuetted")
        player.itemOnObject(emptyAlcove, player.inventory.indexOf("statuette_the_golem"))
        tick(2)
        assertFalse(player.inventory.contains("statuette_the_golem"))
        assertEquals(1, player["golem_statuettestatusd", 0])
        assertEquals("statuette_replaced", player["the_golem", "unstarted"])

        // Stage statuette_replaced -> portal_opened: left, left, right, right
        turn(player, Tile(2719, 4899), Tile(2718, 4899), "golem_statuettea")
        assertEquals(1, player["golem_statuettestatusa", 0])
        turn(player, Tile(2719, 4896), Tile(2718, 4896), "golem_statuetteb")
        assertEquals(1, player["golem_statuettestatusb", 0])
        assertEquals("statuette_replaced", player["the_golem", "unstarted"])
        turn(player, Tile(2724, 4896), Tile(2725, 4896), "golem_statuetted")
        assertEquals(2, player["golem_statuettestatusd", 0])
        assertEquals("portal_opened", player["the_golem", "unstarted"])

        // Stage portal_opened -> demon_dead: Thammaron is already a skeleton
        player.tele(2721, 4911, 0)
        val door = GameObjects.find(Tile(2720, 4912), "golem_portal")
        player.objectOption(door, "Enter")
        tick(2)
        assertEquals("demon_dead", player["the_golem", "unstarted"])
        assertEquals(Tile(3552, 4948), player.tile)

        // The throne gems are the quest's reward for bringing a hammer and chisel
        player.inventory.add("hammer")
        player.inventory.add("chisel")
        player.tele(3551, 4976, 0)
        val throne = GameObjects.find(Tile(3551, 4978), "golem_demon_throne")
        player.itemOnObject(throne, player.inventory.indexOf("chisel"))
        tick(2)
        player.dialogueContinue()
        assertEquals(2, player.inventory.count("ruby"))
        assertEquals(2, player.inventory.count("emerald"))
        assertEquals(2, player.inventory.count("sapphire"))

        // Stage demon_dead -> convince_golem: the golem refuses to believe it
        player.tele(3489, 3090, 0)
        player.npcOption(golem, "Talk-to")
        tick(2)
        player.dialogueContinue(5) // opening line + demonDead
        assertEquals("convince_golem", player["the_golem", "unstarted"])

        // Write new instructions with a phoenix quill and mushroom ink
        player.inventory.add("black_mushroom")
        player.inventory.add("pestle_and_mortar")
        player.inventory.add("vial")
        player.itemOnItem("black_mushroom", "pestle_and_mortar")
        tick(2)
        player.dialogueContinue()
        assertTrue(player.inventory.contains("black_mushroom_ink"))

        player.inventory.add("phoenix_feather")
        player.itemOnItem("black_mushroom_ink", "phoenix_feather")
        tick(2)
        player.dialogueContinue()
        assertTrue(player.inventory.contains("phoenix_quill_pen"))

        player.inventory.add("papyrus")
        player.itemOnItem("papyrus", "phoenix_quill_pen")
        tick(2)
        player.dialogueContinue()
        assertTrue(player.inventory.contains("golem_program"))

        // Stage convince_golem -> completed: open the skull and swap the program
        player.inventory.add("strange_implement")
        player.itemOnNpc(golem, player.inventory.indexOf("strange_implement"))
        tick(2)
        assertTrue(player["golem_head_open", false])

        player.itemOnNpc(golem, player.inventory.indexOf("golem_program"))
        tick(2)
        player.dialogueContinue(3)
        assertEquals("completed", player["the_golem", "unstarted"])
        assertFalse(player.inventory.contains("golem_program"))
        assertEquals(1, player["quest_points", 0])
        assertEquals(1000.0, player.experience.get(Skill.Crafting) - Level.experience(20))
        assertFalse(player.containsMessage("Nothing interesting happens."))
    }

    @Test
    fun `Using the statuette on another alcove does not fill the fourth`() {
        val player = createPlayer(Tile(2719, 4899))
        player["the_golem"] = "find_statuette"
        player.inventory.add("statuette_the_golem")

        // 6307/6308 are shared by all four alcoves, so alcove A must not accept it.
        val alcoveA = GameObjects.find(Tile(2718, 4899), "golem_statuettea")
        player.itemOnObject(alcoveA, player.inventory.indexOf("statuette_the_golem"))
        tick(2)

        assertTrue(player.inventory.contains("statuette_the_golem"))
        assertEquals(0, player["golem_statuettestatusd", 0])
        assertEquals("find_statuette", player["the_golem", "unstarted"])
    }

    @Test
    fun `Crushing a black mushroom without a vial makes no ink`() {
        val player = createPlayer()
        player.inventory.add("pestle_and_mortar")
        player.inventory.add("black_mushroom")

        player.itemOnItem("pestle_and_mortar", "black_mushroom")
        tick(2)
        player.dialogueContinue()

        assertEquals(0, player.inventory.count("black_mushroom_ink"))
        assertEquals(0, player.inventory.count("black_mushroom"))
        assertEquals(0, player.inventory.count("vial"))
    }

    private fun turn(player: Player, from: Tile, tile: Tile, id: String) {
        player.tele(from.x, from.y, 0)
        val alcove = GameObjects.find(tile, id)
        player.objectOption(alcove, "Turn")
        tick(3)
        assertFalse(player.containsMessage("I can't reach that."))
    }
}
