package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player

class FremennikShipmaster : Script {
    init {
        npcOperate("Talk-to", "fremennik_shipmaster_al_kharid") {
            npc<Angry>("You want passage to Daemonheim?")
            choice {
                yesPlease()
                notRightNow()
                daemonheim()
            }
        }

        npcOperate("Sail", "fremennik_shipmaster_al_kharid") {
            sail()
        }
    }

    private fun ChoiceOption.daemonheim() {
        option<Quiz>("Daemonheim?") {
            npc<Pleased>("Yes, the icy peninsula far to the north of here.")
            npc<Pleased>("Ice, snow, harsh winds...")
            npc<Angry>("...and no sand or swamp sludge, clogging up every orifice.")
            npc<Angry>("Are you done with questions? Can we go now?")
            choice {
                yesPlease()
                notRightNow()
                whySoGrumpy()
            }
        }
    }

    private fun ChoiceOption.whySoGrumpy() {
        option<Quiz>("Why are you so grumpy?") {
            npc<Angry>("Grumpy? I should kill you where you stand!")
            npc<Angry>("But that wouldn't help with this damned humidity.")
            npc<Angry>("I need the snow in my boots, the sea wind stinging my face...")
            npc<Quiz>("That's why I want to leave. Are you ready to go to Daemonheim?")
            choice {
                yesPlease()
                notRightNow()
                daemonheim()
            }
        }
    }

    private fun ChoiceOption.notRightNow() {
        option("Not right now, thanks.") {
            npc<Angry>("Well, be on your way then. Leave me in peace!")
        }
    }

    private fun ChoiceOption.yesPlease() {
        option("Yes, please.") {
            npc<Angry>("Well, don't stand around. Get on board.")
            sail()
        }
    }

    private suspend fun Player.sail() {
        open("fade_out")
        delay(4)
        tele(3513, 3693)
        open("fade_in")
    }
}
