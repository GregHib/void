package content.area.misthalin.edgeville.stronghold_of_player_safety

import world.gregs.voidps.engine.Script
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.queue.queue


class StrongholdOfPlayerSafetyGuard : Script {
    init {
        npcOperate("Talk-to", "guard_stronghold_of_player_safety") {
            npc<Neutral>("Can I help you?")
            player<Neutral>("I hope so. What is this place?")
            npc<Neutral>("Above us is the Misthalin Training Centre of Excellence, where young adventurers are taught things that will help keep themselves safe.")
            npc<Shifty>("They say that hidden away somewhere here is the entrance to the old jail, which no doubt has fabulous treasures for those willing to search for them.")
            npc<Neutral>("Together they're called the Stronghold of Player Safety, for historical reasons.")
            player<Quiz>("So what do you do?")
            npc<Happy>("I guard this stairway to make sure that prospective students are ready, and to explain the Report Abusive function.")
            choice {
                option<Quiz>("What is this Report Abuse thing?") {
                    npc<Neutral>("Should you find a player who acts in a way that breaks one of our rules, you should report them.")
                    npc<Neutral>("Reporting is very simple and easy to do. Simply click the Report Abuse button at the bottom of the screen and you will be shown the following screens:")
                    open("report_abuse_select")
                    interfaceOpened("report_abuse_select") {
                        closeDialogue()
                    }
                    interfaceClosed("report_abuse_select") {
                        queue("guard_chat") {
                            npc<Neutral>("On the first page, simply enter the player's name in the box and then click 'Next'")
                            npc<Neutral>("On the following page, click on the offence that the player has commited.")
                            npc<Neutral>("Finally, you'll be given the option to temporarily ignore the player you've reported. That will last until you next log out.")
                            player<Neutral>("Thank you. I'll bear that in mind.")
                            closeDialogue()
                        }
                    }
                }
                option<Neutral>("That's interesting. Goodbye.")
            }
        }
    }
}