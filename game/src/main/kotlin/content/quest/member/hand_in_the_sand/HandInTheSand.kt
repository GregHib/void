package content.quest.member.hand_in_the_sand

import content.entity.obj.door.Door
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.messageScroll
import content.quest.quest
import content.quest.questComplete
import content.quest.questJournal
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.type.Tile

class HandInTheSand : Script {
    init {
        questJournalOpen("hand_in_the_sand") {
            questJournal("Hand in the Sand", journalLines())
        }

        objectOperate("Search", "handsand_desk") {
            val stage = quest("hand_in_the_sand")
            if (stage != "visit_sandy" && stage != "confront_bert") {
                statement("You find nothing of interest.")
                return@objectOperate
            }
            if (ownsItem("sandys_rota")) {
                statement("You already have Sandy's original work rota.")
                return@objectOperate
            }
            if (inventory.spaces < 1) {
                statement("I'd better make room in my inventory first!")
                return@objectOperate
            }
            if (stage == "visit_sandy") {
                set("hand_in_the_sand", "confront_bert")
            }
            addOrDrop("sandys_rota")
            statement(
                "You quickly sift through some of the papers on Sandy's desk and " +
                    "find a work rota for Bert.",
            )
        }

        objectOperate("Open", "magic_guild_door_closed,magic_guild_door_2_closed") { (target) ->
            if (!hasMax(Skill.Magic, 66)) {
                npc<Neutral>(
                    "You need a magic level of 66 for admittance to the guild. The magical " +
                        "energy inside the guild is unsafe for anyone below that level. For any " +
                        "other business please ring for attention.",
                )
                return@objectOperate
            }
            Door.openDoor(this, target)
        }

        itemOption("Activate", "magical_orb") {
            if (quest("hand_in_the_sand") != "activate_orb") {
                message("Nothing interesting happens!")
                return@itemOption
            }
            sound("handsand_orb")
            inventory.replace("magical_orb", "magical_orb_active")
            set("hand_in_the_sand", "interrogate_sandy")
            item(
                item = "magical_orb",
                text = "You rub the magical scrying orb as the Wizard told " +
                    "you, it starts to glow, recording everything it sees and" +
                    "hears, now you can talk to Sandy in Brimhaven.",
            )
        }

        itemOption("Read", "berts_rota") {
            showRota(
                title = "Bert's Rota - Copy",
                week1 = "6am-10pm",
                week2 = "6am-10pm",
                week3 = "6am-10pm",
                week4 = "6am-10pm",
                week5 = "6am-10pm",
                week6 = "6am-10pm",
            )
        }

        itemOption("Read", "sandys_rota") {
            showRota(
                title = "Bert's Rota - Original",
                week1 = "9am-6pm",
                week2 = "9am-6pm",
                week3 = "9am-6pm",
                week4 = "9am-6pm",
                week5 = "9am-6pm",
                week6 = "6am-10pm",
            )
        }

        itemOnItem("bottled_water", "redberries") { _, _ ->
            inventory.remove("bottled_water")
            inventory.remove("redberries")
            inventory.add("redberry_juice")
            set("handsand_serum", 2)
            message("Now you just need to add white berries to make the pink dye.")
        }

        itemOnItem("bottled_water", "white_berries") { _, _ ->
            message("You'll need to use red berries first.")
        }

        itemOnItem("redberry_juice", "white_berries") { _, _ ->
            inventory.remove("redberry_juice")
            inventory.remove("white_berries")
            inventory.add("pink_dye")
            set("handsand_serum", 3)
        }

        itemOnItem("pink_dye", "lantern_lens") { _, _ ->
            if (quest("hand_in_the_sand") != "make_serum") {
                message("Nothing interesting happens.")
                return@itemOnItem
            }
            inventory.remove("pink_dye")
            inventory.remove("lantern_lens")
            inventory.add("rose_tinted_lens")
            set("handsand_serum", 4)
            item(
                item = "rose_tinted_lens",
                text = "You have successfully made the rose tinted lens!",
            )
        }

        itemOnItem("sand", "truth_serum") { _, _ ->
            statement("Perhaps you should let Betty do that, it looks tricky.")
        }

        itemOnObjectApproach("rose_tinted_lens", "handsand_counter") {
            steps.clear()
            if (!get("handsand_counter_multi", false)) {
                message("Nothing interesting happens.")
                return@itemOnObjectApproach
            }
            if (GameObjects.findOrNull(BETTY_DOOR, "door_668_closed") != null) {
                statement("You will need to open the door to let the light in first!")
                return@itemOnObjectApproach
            }
            if (tile != BETTY_DOORWAY) {
                message("You need to be standing in the doorway.")
                return@itemOnObjectApproach
            }
            set("handsand_counter_multi", false)
            set("handsand_serum", 5)
            sound("handsand_vialshatter")
            inventory.remove("rose_tinted_lens")
            inventory.add("truth_serum")
            item(
                item = "truth_serum",
                text = "As you focus the light on the vial and Betty pours the " +
                    "potion in, the lens heats up and shatters. After a few" +
                    "seconds Betty hands you the vial of Truth Serum.",
            )
        }

        itemOnObjectOperate("truth_serum", "handsand_coffee") {
            if (get("handsand_serum", 0) != SERUM_FINISHED) {
                statement("You'll need to have completed the truth serum before doing that!")
                return@itemOnObjectOperate
            }
            if (quest("hand_in_the_sand") != "drug_coffee") {
                statement("You'll need to distract Sandy before doing that.")
                return@itemOnObjectOperate
            }
            set("hand_in_the_sand", "activate_orb")
            set("handsand_sandy_multi", 0)
            sound("handsand_serum")
            inventory.remove("truth_serum")
            statement(
                "You pour the serum into Sandy's coffee, then a little while later " +
                    "watch him drink it.",
            )
        }
    }

    private fun Player.showRota(
        title: String,
        week1: String,
        week2: String,
        week3: String,
        week4: String,
        week5: String,
        week6: String,
    ) {
        messageScroll(
            listOf(
                "Sandy's Sand Corp - Brimhaven",
                title,
                "Week 1   -   $week1   -   50gps",
                "Week 2   -   $week2   -   50gps",
                "Week 3   -   $week3   -   50gps",
                "Week 4   -   $week4   -   50gps",
                "Week 5   -   $week5   -   50gps",
                "Week 6   -   $week6   -   50gps",
            ),
            handwriting = true,
        )
    }

    private fun Player.journalLines(): List<String> {
        val progress = questStage("hand_in_the_sand")
        val lines = mutableListOf<String>()

        if (progress == UNSTARTED) {
            lines += "<navy>I can start this quest by speaking to <maroon>Bert<navy> in <maroon>Yanille<navy> in the"
            lines += "<navy>house near the <maroon>Sandpit<navy>."
            lines += ""
            lines += "<navy>Before I begin I will need to:"
            lines += if (hasMax(Skill.Thieving, 17)) "<str>Have level 17 Thieving." else "<navy>Have level 17 <maroon>Thieving<navy>."
            lines += if (hasMax(Skill.Crafting, 49)) "<str>Have level 49 Crafting." else "<navy>Have level 49 <maroon>Crafting<navy>."
            return lines
        }

        lines += "<str>Bert the sandpit worker in Yanille has asked me to"
        lines += "<str>investigate the hand that he found in the sand."
        if (progress < ASK_WIZARDS) {
            lines += "<navy>I need to speak to the <maroon>Guard Captain<navy> who is in the <maroon>Dragon"
            lines += "<maroon>Inn<navy> south of the <maroon>sandpit<navy>."
        }
        if (progress >= ASK_WIZARDS) {
            lines += "<str>I have spoken to the Guard Captain."
            if (progress < BERT_HOURS) {
                lines += "<navy>I need to see if the <maroon>Wizards<navy> in the guild in Yanille know"
                lines += "<navy>anything about the <maroon>hand<navy>."
            }
        }
        if (progress >= BERT_HOURS) {
            lines += "<str>I have shown the hand to the Wizards in Yanille."
            if (progress < VISIT_SANDY) {
                lines += "<navy>Find out why <maroon>Bert's<navy> hours have changed."
            }
        }
        if (progress >= VISIT_SANDY) {
            lines += "<str>I have Bert's copy of the Rota."
            if (progress < CONFRONT_BERT) {
                lines += "<navy>I should ask <maroon>Sandy<navy> in the <maroon>Sand Corp<navy> Offices in <maroon>Brimhaven<navy>"
                lines += "<navy>about Bert's rota."
            }
        }
        if (progress >= CONFRONT_BERT) {
            lines += "<str>I have Sandy's copy of the Rota."
            if (progress < DELIVER_SCROLL) {
                lines += "<navy>Show <maroon>Bert<navy> the changes in his hours."
            }
        }
        if (progress == DELIVER_SCROLL) {
            lines += "<navy>Ring the bell at the Wizard Guild in <maroon>Yanille<navy> and give <maroon>scroll<navy> to"
            lines += "<navy><maroon>Zavistic Rarve<navy>."
        }
        if (progress >= MAKE_SERUM) {
            lines += "<str>I have taken the scroll to Zavistic Rarve."
        }
        if (progress == MAKE_SERUM) {
            lines += serumLines()
        }
        if (progress == DISTRACT_SANDY) {
            lines += "<navy>Find a way to make <maroon>Sandy<navy> drink the <maroon>Truth Serum<navy>."
        }
        if (progress >= DRUG_COFFEE) {
            lines += "<str>I have distracted Sandy successfully."
        }
        if (progress >= ACTIVATE_ORB) {
            lines += "<str>I have drugged Sandy's coffee."
            if (progress < INTERROGATE_SANDY) {
                lines += "<navy>I must activate the magical scrying orb to record the"
                lines += "<navy>interview with <maroon>Sandy<navy>."
            }
        }
        if (progress >= INTERROGATE_SANDY) {
            lines += "<str>I have activated the magical scrying orb."
            if (progress < RETURN_ORB) {
                lines += "<navy>Ask <maroon>Sandy<navy> about <maroon>Hand in the Sand<navy>."
            }
        }
        if (progress >= RETURN_ORB) {
            lines += "<str>I have interogated Sandy."
            if (progress < GATHER_RUNES) {
                lines += "<navy>Return the information gathered to <maroon>Zavistic Rarve<navy> in"
                lines += "<navy><maroon>Yanille<navy> wizard guild."
            }
        }
        if (progress >= GATHER_RUNES) {
            lines += "<str>I have returned the information from the orb."
            if (progress < SEARCH_ENTRANA) {
                lines += "<navy>Find and return <maroon>5 earth runes<navy> and <maroon>a bucket of sand<navy> to"
                lines += "<navy><maroon>Zavistic Rarve<navy> in Yanille."
            }
        }
        if (progress >= SEARCH_ENTRANA) {
            lines += "<str>The Sandpit has been enchanted."
            if (progress < RETURN_HEAD) {
                lines += "<navy>Visit the <maroon>sandpit<navy> on the island of <maroon>Entrana<navy> and return any"
                lines += "<navy>other wizard parts."
            }
        }
        if (progress >= RETURN_HEAD) {
            lines += "<str>I have retrieved the head of a wizard."
            if (progress < COMPLETED) {
                lines += "<navy>Return the <maroon>wizard head<navy> to <maroon>Zavistic Rarve<navy> in <maroon>Yanille<navy>."
            }
        }
        if (progress >= COMPLETED) {
            lines += ""
            lines += "<str>The dead wizard has been buried and Sandy arrested for"
            lines += "<str>murder."
            lines += ""
            lines += "<red>QUEST COMPLETE!"
            lines += "<navy>For an extra reward I must speak to Bert and tell him about"
            lines += "<navy>his pension."
            if (get("handsand_employed_bert", false)) {
                lines += ""
                lines += "<navy>Every day I may ask <maroon>Bert<navy> to transport some sand to my"
                lines += "<navy>bank."
            }
        }
        return lines
    }

    private fun Player.serumLines(): List<String> {
        val serum = get("handsand_serum", 0)
        if (serum == 0) {
            return listOf(
                "<maroon>Betty<navy> in <maroon>Port Sarim<navy> will guide you on how to make some",
                "<maroon>Truth Serum<navy>.",
            )
        }
        val lines = mutableListOf(
            "<maroon>Betty<navy> has told me what I need to do to make the <maroon>Truth",
            "<maroon>Serum<navy>:",
        )
        when {
            serum >= 5 -> lines += "<navy>Talk to <maroon>Betty<navy> to find out how to use the <maroon>Truth Serum<navy>."
            serum >= 4 -> lines += "<navy>Talk to <maroon>Betty<navy> to find out how to finish the <maroon>Truth Serum<navy>."
            else -> {
                lines += if (ownsItem("lantern_lens")) "<str>I have a bullseye lens" else "<navy>I need to <maroon>craft<navy> a bullseye lens"
                lines += if (serum >= 2) {
                    "<str>I have made some redberry juice"
                } else {
                    "<navy>I need to make some <maroon>redberry juice<navy> in the bottle <maroon>Betty<navy> gave me"
                }
                lines += if (serum >= 3) "<str>I have made the pink dye" else "<navy>I need to make some <maroon>pink dye<navy>"
                lines += "<navy>I need to make a <maroon>rose tinted lens<navy>"
            }
        }
        return lines
    }

    private companion object {
        const val UNSTARTED = 0
        const val ASK_WIZARDS = 20
        const val BERT_HOURS = 30
        const val VISIT_SANDY = 40
        const val CONFRONT_BERT = 50
        const val DELIVER_SCROLL = 60
        const val MAKE_SERUM = 70
        const val DISTRACT_SANDY = 80
        const val DRUG_COFFEE = 90
        const val ACTIVATE_ORB = 100
        const val INTERROGATE_SANDY = 110
        const val RETURN_ORB = 120
        const val GATHER_RUNES = 130
        const val SEARCH_ENTRANA = 140
        const val RETURN_HEAD = 150
        const val COMPLETED = 160

        const val SERUM_FINISHED = 6

        val BETTY_DOOR = Tile(3017, 3259, 0)
        val BETTY_DOORWAY = Tile(3016, 3259, 0)
    }
}

suspend fun Player.sendHandQuestReward() {
    set("hand_in_the_sand", "completed")
    jingle("quest_complete_1")
    exp(Skill.Crafting, 9000.0)
    exp(Skill.Thieving, 1000.0)
    inc("quest_points")
    AuditLog.event(this, "quest_completed", "hand_in_the_sand")

    set("handsand_question1", false)
    set("handsand_question2", false)
    set("handsand_question3", false)
    set("handsand_tele", true)
    set("handsand_serum", 6)
    set("handsand_sandy_multi", 2)
    set("handsand_coffee_multi", 1)
    set("handsand_counter_multi", false)
    refreshQuestJournal()
    questComplete(
        "Hand in the Sand",
        "1 Quest Point",
        "9,000 Crafting XP",
        "1,000 Thieving XP",
        "Wizards' Guild Rune Store access",
        "A secret reward from Bert",
        item = "sandy_hand",
    )
}
