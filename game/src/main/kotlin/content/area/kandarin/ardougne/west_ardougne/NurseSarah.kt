package content.area.kandarin.ardougne.west_ardougne

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class NurseSarah : Script {

    init {
        npcOperate("Talk-to", "nurse_sarah_west_ardougne") {
            when (questStage("biohazard")) {
                in 6..7 -> {
                    player<Happy>("Hello nurse.")
                    npc<Neutral>("Oh hello there.")
                    npc<Neutral>("I'm afraid I can't stop and talk, a group of mourners have become ill with food poisoning. I need to go over and see what I can do.")
                    player<Confused>("Hmmm, strange that!")
                }
                in 0..5 -> {
                    player<Happy>("Hello nurse.")
                    npc<Sad>("I don't know how much longer I can cope here.")
                    player<Quiz>("What? Is the plague getting to you?")
                    npc<Confused>("No, strangely enough the people here don't seem to be affected. It's just the awful living conditions that are making people ill.")
                    player<Confused>("I was under the impression that everyone here was affected.")
                    npc<Neutral>("Me too, but that doesn't seem to be the case.")
                }
                else -> {
                    player<Neutral>("Hello there.")
                    npc<Neutral>("Hello my dear, how are you feeling?")
                    player<Neutral>("I'm ok thanks.")
                    npc<Happy>("Well in that case I'd better get back to work. Take care.")
                    player<Happy>("You too.")
                }
            }
        }

        objectOperate("Open", "bio_nurses_cupboard_shut") { (target) ->
            message("You open the cupboard.")
            sound("cupboard_open")
            anim("human_opencupboard")
            target.replace("bio_nurses_cupboard_open", ticks = TimeUnit.MINUTES.toTicks(1))
        }

        objectOperate("Close", "bio_nurses_cupboard_open") { (target) ->
            message("You close the cupboard.")
            sound("cupboard_close")
            anim("human_opencupboard")
            target.remove()
        }

        objectOperate("Search", "bio_nurses_cupboard_open") {
            searchCupboard()
        }
    }

    private fun Player.searchCupboard() {
        message("You search the cupboard...")
        if (questStage("biohazard") < 6 || ownsItem("doctors_gown")) {
            message("but you find nothing of interest.")
            return
        }
        addOrDrop("doctors_gown")
        message("and find a doctor's gown.")
    }
}
