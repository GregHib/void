package content.quest.member.the_golem

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.messageScroll
import content.quest.questJournal
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class TheGolem : Script {

    init {
        questJournalOpen("the_golem") {
            questJournal("The Golem", journalLines())
        }

        objectOperate("Climb-down", "sote_warped_library_floor_lvl2_centre_01") {
            tele(2721, 4886)
            set("golem_seen_underground", true)
        }

        objectOperate("Climb-up", "golem_insidestairs_base") {
            tele(3491, 3090)
        }

        objectOperate("Search", "sote_pillar_ithell_b_0") {
            message("You search the bookcase...")
            delay(3)
            if (get("golem_b", 0) < 2 || inventory.contains("varmens_notes")) {
                message("You find nothing of interest.")
                return@objectOperate
            }
            if (inventory.isFull()) {
                message("You find Varmen's expedition notes, but don't have room to take them.")
                return@objectOperate
            }
            set("golem_b", 3)
            inventory.add("varmens_notes")
            item("varmens_notes", "You find Varmen's expedition notes.")
        }

        itemOption("Read", "letter_the_golem") {
            readVarmensLetter()
            if (get("golem_b", 0) == 0) {
                set("golem_b", 1)
            }
        }

        objectOperate("Pick", "golem_black_mushrooms") {
            addOrDrop("black_mushroom")
            delay(1)
            sound("pick")
            anim("take")
            item("black_mushroom", "You pick a mushroom.")
        }

        // vm_timeline_no_display is the empty-case transform for every museum display,
        // so scope both options to the terracotta statue's own base object.
        objectOperate("Study", "vm_timeline_terracotta_statue_multi,vm_timeline_no_display") { (target) ->
            if (target.id != "vm_timeline_terracotta_statue") {
                noInterest()
                return@objectOperate
            }
            studyDisplayCase()
        }

        objectOperate("Open", "vm_timeline_terracotta_statue_multi") {
            if (!inventory.contains("display_cabinet_key")) {
                message("The cabinet is locked.")
                return@objectOperate
            }
            openDisplayCase()
        }

        itemOnObjectOperate("display_cabinet_key", "vm_timeline_terracotta_statue_multi") {
            openDisplayCase()
        }

        // The filled forms are registered too so the interaction stays valid after the
        // varbit transforms the alcove mid-tick; they are shared by all four alcoves,
        // so the base object has to be checked.
        itemOnObjectOperate("statuette_the_golem", "golem_statuette_missing,golem_statuette_facing_left,golem_statuette_facing_right") { (target) ->
            if (target.id != "golem_statuetted") {
                noInterest()
                return@itemOnObjectOperate
            }
            if (questStage("the_golem") >= 4) {
                message("There is already a statuette in the alcove.")
                return@itemOnObjectOperate
            }
            if (!inventory.remove("statuette_the_golem")) {
                return@itemOnObjectOperate
            }
            set("golem_statuettestatusd", 1)
            set("the_golem", "statuette_replaced")
            message("You insert the statuette into the alcove.")
        }

        objectOperate("Turn", "golem_statuette_facing_right,golem_statuette_facing_left") { (target) ->
            if (questStage("the_golem") >= 5) {
                message("The statuette is now locked in place.")
                return@objectOperate
            }
            val varbit = VariableDefinitions.getVarbit(target.def.varbit) ?: return@objectOperate
            turnStatuette(varbit)
        }

        objectOperate("Open", "golem_demon_door") {
            message("You can't find any way to open the door.")
        }

        objectOperate("Enter", "golem_demon_door_always_open") {
            message("You step into the portal.")
            if (questStage("the_golem") == 5) {
                message("This room is dominated by a colossal horned skeleton!")
                set("the_golem", "demon_dead")
            }
            tele(3552, 4948)
        }

        objectOperate("Enter", "golem_demon_portal") {
            message("You step into the portal.")
            tele(2721, 4911)
        }

        itemOnObjectOperate("hammer,chisel", "golem_throne_withgems,golem_throne_nogems") {
            removeThroneGems()
        }

        itemOption("Eat", "black_mushroom") { (_, slot) ->
            inventory.remove(slot, "black_mushroom")
            message("Eugh! It tastes horrible, and stains your fingers black.")
        }

        // Not a crushing.recipes entry: the ink needs a vial the player didn't click on,
        // which make-x can't ask for, and the item empties back into one.
        itemOnItem("black_mushroom", "pestle_and_mortar") { _, _ ->
            if (!inventory.remove("black_mushroom")) {
                return@itemOnItem
            }
            if (!inventory.remove("vial")) {
                statement("You crush the mushroom, but you have no vial to put the ink in and it goes everywhere!")
                return@itemOnItem
            }
            inventory.add("black_mushroom_ink")
            item("black_mushroom_ink", "You crush the mushroom and pour the juice into a vial.")
        }

        itemOnItem("black_mushroom_ink", "phoenix_feather") { _, _ ->
            inventory.remove("black_mushroom_ink")
            inventory.remove("phoenix_feather")
            inventory.add("phoenix_quill_pen")
            item("phoenix_quill_pen", "You dip the phoenix feather into the ink.")
        }

        itemOnItem("papyrus", "phoenix_feather") { _, _ ->
            message("You will need some kind of ink to write.")
        }

        itemOnItem("papyrus", "phoenix_quill_pen") { _, _ ->
            if (questStage("the_golem") != 7) {
                message("You don't know what to write.")
                return@itemOnItem
            }
            inventory.remove("papyrus")
            inventory.add("golem_program")
            item("golem_program", "You write on the papyrus:<br>YOUR TASK IS DONE")
        }
    }

    private fun Player.studyDisplayCase() {
        if (!open("vm_timeline")) {
            return
        }
        val stolen = get("golem_retrieved_statuette", false)
        sendScript("museum_rotate_display", 0, 5, 0, 534 shl 16 or 50)
        interfaces.sendModel("vm_timeline", "vm_timeline_terracotta_statue_model", if (stolen) 25568 else 25576)
        interfaces.sendText("vm_timeline", "display_num", "30")
        interfaces.sendText(
            "vm_timeline",
            "vm_timeline_text",
            "3rd Age - yr 3000-4000<br><br>This " +
                "statuette was found in an underground temple in the ruined city of Uzer, which was destroyed late " +
                "in the 3rd Age, suddenly, due to causes unknown. It probably represents one of the clay golems " +
                "that the craftsmen of the city built as warriors and servants. The statuette was originally part " +
                "of a mechanism whose purpose is unknown." +
                (if (stolen) "<br><br>Recently this display was stolen and its whereabouts are unknown." else ""),
        )
    }

    private suspend fun Player.openDisplayCase() {
        if (get("golem_retrieved_statuette", false) || ownsItem("statuette_the_golem")) {
            message("You have already taken the statuette.")
            return
        }
        if (inventory.isFull()) {
            message("You do not have enough space for the statuette in your backpack.")
            return
        }
        set("golem_retrieved_statuette", true)
        addOrDrop("statuette_the_golem")
        item("statuette_the_golem", "You open the cabinet and retrieve the statuette.")
    }

    private suspend fun Player.turnStatuette(varbit: String) {
        val left = get(varbit, 0) == 1
        val rightValue = if (varbit == "golem_statuettestatusd") 2 else 0
        set(varbit, if (left) rightValue else 1)
        message("You turn the statuette to the " + (if (left) "right." else "left."))
        if (templeDoorSolved()) {
            message("The door grinds open.")
            sound("golem_demondoor")
            set("the_golem", "portal_opened")
        } else {
            sound("turn_statue")
        }
        delay(1)
    }

    private fun Player.templeDoorSolved(): Boolean = get("golem_statuettestatusa", 0) == 1 &&
        get("golem_statuettestatusb", 0) == 1 &&
        get("golem_statuettestatusc", 0) == 0 &&
        get("golem_statuettestatusd", 0) == 2

    private suspend fun Player.removeThroneGems() {
        if (get("golem_throne_gems", false)) {
            message("You have already removed the gems from the throne.")
            return
        }
        if (!inventory.contains("hammer") || !inventory.contains("chisel")) {
            message("You'll need a hammer as well as a chisel to get the gems.")
            return
        }
        if (inventory.spaces < 6) {
            message("You don't have enough free space to remove all six gems.")
            return
        }
        set("golem_throne_gems", true)
        inventory.add("ruby", 2)
        inventory.add("emerald", 2)
        inventory.add("sapphire", 2)
        anim("pick_pocket")
        sound("pick")
        item("ruby", "You prise the gems from the demon's throne.")
    }

    private fun Player.readVarmensLetter() {
        messageScroll(
            listOf(
                "Dearest Varmen,",
                "I hope this finds you well. Here are the books you asked for.",
                "There has been an exciting development closer to home --",
                "another city from the same period has been discovered east",
                "of Varrock, and we are starting a huge excavation project",
                "here. I don't know if the museum will be able to finance your",
                "expedition as well as this one, so I fear your current trip will be",
                "the last.",
                "May Saradomin grant you a safe journey home.",
                "Your loving Elissa.",
            ),
        )
    }

    private fun Player.journalLines(): List<String> {
        val progress = questStage("the_golem")
        val lines = mutableListOf<String>()
        if (progress == 0) {
            val crafting = hasMax(Skill.Crafting, 20)
            val thieving = hasMax(Skill.Thieving, 25)
            lines += "<navy>I can start this quest by talking to the golem who is in the"
            lines += "<navy>ruined city of <maroon>Uzer<navy>, which is in the desert to the east of"
            lines += "<navy>the <maroon>Shantay Pass<navy>."
            lines += "<navy>I will need to have:"
            lines += "${if (crafting) "<str>" else "<maroon>"}level 20 crafting."
            lines += "${if (thieving) "<str>" else "<maroon>"}level 25 thieving."
            if (crafting && thieving) {
                lines += "<navy>I have all the skill requirements to start this quest."
            }
            return lines
        }
        lines += "<str>I have spoken to the golem."
        if (progress < 2) {
            lines += "<navy>The <maroon>golem<navy> asked me to repair him."
            return lines
        }
        lines += "<str>I have repaired the golem."
        if (progress >= 5) {
            lines += "<str>The golem asked me to open the portal so it can defeat a"
            lines += "<str>demon."
            lines += "<str>To open the portal I will need to find a missing statuette."
        } else {
            lines += "<navy>The golem asked me to open the <maroon>portal<navy> so it can defeat a"
            lines += "<navy><maroon>demon<navy>."
            if (get("golem_seen_underground", false)) {
                lines += "<navy>To open the portal I will need to find a missing <maroon>statuette<navy>."
            } else {
                lines += "<navy>I should go down the stairs in <maroon>Uzer <navy>and work out how to open."
                lines += "<navy>the <maroon>portal<navy>."
            }
        }
        lines += statuetteHuntLines()
        if (progress >= 6) {
            lines += "<str>I have visited the demon's lair and seen its skeleton."
        } else if (progress == 5) {
            lines += "<navy>I should find out what happened to the <maroon>demon<navy>."
        }
        if (progress == 6) {
            lines += "<navy>I should inform the <maroon>golem<navy> that the <maroon>demon<navy> is dead."
        }
        if (progress >= 7) {
            lines += "<str>I told the golem that the demon was dead, but it did not"
            lines += "<str>believe me!"
        }
        if (progress == 7) {
            lines += "<navy>I should find some way to convince the <maroon>golem<navy> that its task"
            lines += "<navy>is done."
        }
        if (progress >= 10) {
            lines += "<str>I reprogrammed the golem so that it knows its task is"
            lines += "<str>complete."
            lines += "<col=ff0000>QUEST COMPLETE!"
        }
        return lines
    }

    private fun Player.statuetteHuntLines(): List<String> {
        val lines = mutableListOf<String>()
        val statue = get("golem_b", 0)
        if (statue == 0) {
            return lines
        }
        if (statue == 1) {
            lines += "<navy>Maybe I should speak to <maroon>Elissa<navy> at the <maroon>Digsite<navy> about the"
            lines += "<navy>letter I found in Uzer."
            return lines
        }
        lines += "<str>I talked to Elissa about the letter from the desert."
        if (statue == 2) {
            lines += "<navy>Elissa told me that <maroon>Varmen's expedition notes<navy> are in the"
            lines += "<navy>library in the <maroon>Exam Centre<navy>."
            return lines
        }
        lines += "<str>I took Varmen's notes from the Digsite Exam Centre."
        val progress = questStage("the_golem")
        if (progress >= 4) {
            lines += "<str>I have retrieved the missing statuette from the Varrock"
            lines += "<str>Museum and repaired the puzzle in the temple."
            if (progress >= 5) {
                lines += "<str>I have opened the portal."
            }
            return lines
        }
        if (progress < 3) {
            lines += "<navy>According to <maroon>Varmen's notes<navy>, he removed the <maroon>statuette<navy>"
            lines += "<navy>during his expedition."
            return lines
        }
        if (get("golem_retrieved_statuette", false)) {
            lines += "<str>I have retrieved the missing statuette from the Varrock"
            lines += "<str>Museum."
            lines += "<navy>I should take the <maroon>statuette<navy> to Uzer and put it back in the"
            lines += "<navy>temple."
            return lines
        }
        lines += "<navy>To open the portal I will need the missing statuette which is"
        lines += "<navy>in the <maroon>Varrock Museum<navy>."
        return lines
    }
}
