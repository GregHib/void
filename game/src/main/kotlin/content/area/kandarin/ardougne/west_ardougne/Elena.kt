package content.area.kandarin.ardougne.west_ardougne

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.startQuest
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open

class Elena : Script {

    init {
        npcOperate("Talk-to", "elenap_vis") {
            player<Happy>("Hi, you're free to go! Your kidnappers don't seem to be about right now.")
            npc<Neutral>("Thank you, being kidnapped was so inconvenient. I was on my way back to East Ardougne with some samples, I want to see if I can diagnose a cure for this plague.")
            player<Neutral>("Well you can leave via the manhole in the middle of the city.")
            npc<Neutral>("Go and see my father, I'll make sure he adequately rewards you. Now I'd better leave while I still can.")
            open("fade_out")
            delay(4)
            set("plaguecity_can_see_edmond_up_top", false)
            set("plaguecity_elena_at_home", true)
            set("plague_city", "freed_elena")
            delay(3)
            open("fade_in")
        }

        npcOperate("Talk-to", "elena2_vis") {
            when (quest("biohazard")) {
                "unstarted" -> {
                    player<Happy>("Good day to you, Elena.")
                    npc<Happy>("You too, thanks for freeing me.")
                    npc<Sad>("It's just a shame the mourners confiscated my equipment.")
                    player<Quiz>("What did they take?")
                    npc<Neutral>("My distillator. I can't test any plague samples without it. They're holding it in the Mourner Headquarters in West Ardougne.")
                    npc<Quiz>("I must somehow retrieve that distillator if I am to find a cure for this awful affliction. Do you think you could help me?")
                    if (startQuest("biohazard")) {
                        // todo add quest
                    } else {
                        player<Neutral>("I'm busy at the moment, I'm afraid.")
                        npc<Neutral>("Fair enough.")
                    }
                }
                "started" -> started()
                else -> completed()
            }
        }
    }

    suspend fun started() {
    }

    suspend fun completed() {
    }
}
