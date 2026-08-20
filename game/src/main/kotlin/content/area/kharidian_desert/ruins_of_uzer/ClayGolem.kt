package content.area.kharidian_desert.ruins_of_uzer

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Unamused
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questComplete
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
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue

/**
 * Npc 1907 transforms on the golem_clay varbit, so every handler registers against
 * the broken/damaged/repaired forms the player actually sees rather than the base.
 */
class ClayGolem : Script {

    init {
        npcOperate("Talk-to", "clay_golem_ruins_of_uzer*") {
            when (val progress = questStage("the_golem")) {
                0 -> {
                    npc<Sad>("Damage... severe...<br>task... incomplete...")
                    choice {
                        tryToRepair()
                        noConvo()
                    }
                }
                1 -> npc<Sad>("Damage... severe...")
                in 2..7 -> {
                    npc<Sad>("My task is incomplete. You must open the portal so I can defeat the great demon.")
                    when (progress) {
                        5 -> portalOpened()
                        6 -> demonDead()
                        7 -> alreadyTold()
                        else -> golemQuestions()
                    }
                }
                else -> npc<Neutral>("Thank you for helping me. A golem can have no greater satisfaction than knowing that its task is complete.")
            }
        }

        itemOnNPCOperate("clay", "clay_golem_ruins_of_uzer*") {
            message("The clay is not soft enough to stick to the golem.")
        }

        itemOnNPCOperate("soft_clay", "clay_golem_ruins_of_uzer*") { (_, _, slot) ->
            when (questStage("the_golem")) {
                0 -> message("Maybe you should ask the golem first!")
                1 -> repairGolem(slot)
                else -> message("You have already repaired the golem.")
            }
        }

        itemOnNPCOperate("strange_implement", "clay_golem_ruins_of_uzer*") {
            if (get("golem_head_open", false)) {
                message("The golem's head is already open.")
                return@itemOnNPCOperate
            }
            message("You insert the key and the golem's skull hinges open.")
            set("golem_head_open", true)
            queue("Golem Head Close", 12) {
                message("The golem's skull shuts automatically.")
                set("golem_head_open", false)
            }
        }

        itemOnNPCOperate("golem_program", "clay_golem_ruins_of_uzer*") {
            reprogram()
        }
    }

    private fun ChoiceOption.tryToRepair() {
        option("Shall I try to repair you?") {
            set("the_golem", "repair_golem")
            refreshQuestJournal()
            player<Quiz>("Shall I try to repair you?")
            npc<Happy>("Repairs... needed...")
        }
    }

    private fun ChoiceOption.noConvo() {
        option("I'm not going to find a conversation here!") {
            player<Unamused>("I'm not going to find a conversation here!")
            npc<Angry>("Graar!")
            npc<Angry>("Must... not... injure... human...")
        }
    }

    private fun ChoiceOption.openPortal() {
        option("How do I open the portal?") {
            player<Quiz>("How do I open the portal?")
            npc<Neutral>("The four statuettes in the temple must be turned to the correct pattern.")
            npc<Neutral>("I do not know the pattern. Golems are not permitted to open the portal.")
        }
    }

    private fun ChoiceOption.defeatDemon() {
        option("What makes you think you can defeat the demon?") {
            player<Quiz>("What makes you think you can defeat the demon?")
            npc<Neutral>("If not I, then who else?  No living being can destroy the demon. That is why the golems were created in the first place.")
            npc<Neutral>("But the demon was badly wounded and elder-demons heal very slowly indeed. It was almost dead when it retreated to its own dimension.")
            npc<Neutral>("Now that I am repaired, I will be able to destroy it easily!")
        }
    }

    private fun ChoiceOption.leave() {
        option("I'll get right on it.") {
            player<Happy>("I'll get right on it.")
        }
    }

    private suspend fun Player.golemQuestions() {
        choice {
            openPortal()
            defeatDemon()
            leave()
        }
    }

    private suspend fun Player.portalOpened() {
        player<Neutral>("I opened the portal.")
        npc<Neutral>("Golems cannot pass through the portal. But the demon will soon emerge. I must ready myself for combat!")
    }

    private suspend fun Player.demonDead() {
        player<Happy>("It's okay, the demon is dead!")
        npc<Confused>("The demon must be defeated...")
        player<Quiz>("No, you don't understand. I saw the demon's skeleton. It must have died of its wounds.")
        npc<Confused>("Demon must be defeated! Task incomplete.")
        set("the_golem", "convince_golem")
        refreshQuestJournal()
    }

    private suspend fun Player.alreadyTold() {
        player<Angry>("I already told you, he's dead!")
        npc<Sad>("Task incomplete.")
        player<Neutral>("Oh, how am I going to convince you?")
    }

    private suspend fun Player.repairGolem(slot: Int) {
        if (!hasMax(Skill.Crafting, 20)) {
            message("You need level 20 crafting to repair the golem.")
            return
        }
        if (!inventory.remove(slot, "soft_clay")) {
            return
        }
        anim("pick_pocket")
        sound("golem_repairclay", delay = 5)
        inc("golem_clay")
        when (get("golem_clay", 0)) {
            1 -> item("soft_clay", "You apply some clay to the golem's wounds. The clay begins to harden in the hot sun.")
            2 -> item("soft_clay", "You fix the golem's legs.")
            3 -> item("soft_clay", "The golem is nearly whole.")
            4 -> repairComplete()
        }
    }

    private suspend fun Player.repairComplete() {
        set("the_golem", "open_portal")
        refreshQuestJournal()
        item("soft_clay", "You repair the golem with a final piece of clay.")
        npc<Confused>("Damage repaired...")
        npc<Happy>("Thank you. My body and mind are fully healed.")
        npc<Neutral>("Now I must complete my task by defeating the great enemy.")
        player<Quiz>("What enemy?")
        npc<Neutral>("A great demon. It broke through from its dimension to attack the city.")
        npc<Neutral>("The golem army was created to fight it. Many were destroyed, but we drove the demon back!")
        npc<Neutral>("The demon is still wounded. You must open the portal so that I can strike the final blow and complete my task.")
    }

    private suspend fun Player.reprogram() {
        if (!get("golem_head_open", false)) {
            message("You can't see a way to put the instructions in the golem's skull.")
            return
        }
        if (questStage("the_golem") == 10) {
            message("You have already reprogrammed the golem.")
            return
        }
        sound("golem_program")
        npc<Confused>("New instructions...<br>Updating program...")
        npc<Happy>("Task complete!")
        npc<Happy>("Thank you. Now my mind is at rest.")
        inventory.remove("golem_program")
        set("the_golem", "completed")
        jingle("quest_complete_1")
        inc("quest_points")
        AuditLog.event(this, "quest_completed", "the_golem")
        refreshQuestJournal()
        questComplete(
            "The Golem",
            "1 Quest Point",
            "1,000 Crafting XP",
            "1,000 Thieving XP",
            item = "statuette_the_golem",
        )
        exp(Skill.Crafting, 1000.0)
        exp(Skill.Thieving, 1000.0)
    }
}
