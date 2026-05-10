package content.area.wilderness.daemonheim

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player

class FremennikShipmaster : Script {
    init {
        npcOperate("Talk-to", "fremennik_shipmaster_daemonheim") {
            npc<Happy>("Do you want to set sail?")
            choice {
                yesPlease()
                notRightNow()
                option("You look happy.") {
                    npc<Laugh>("Indeed, brother! I simply can't get enough of this place.")
                    npc<Happy>("The brisk sea air, the refreshing chill. It really gets the blood pumping.")
                    npc<Happy>("Anyway, listen to me ramble. Are we to set sail?")
                    choice {
                        yesPlease()
                        notRightNow()
                    }
                }
            }
        }

        npcOperate("Sail", "fremennik_shipmaster_daemonheim") {
            sail()
        }
    }

    private fun ChoiceOption.notRightNow() {
        option("Not right now, thanks.") {
            npc<Neutral>("Suit yourself. I'll be here soaking up the atmosphere if you change your mind.")
        }
    }

    private fun ChoiceOption.yesPlease() {
        option("Yes, please.") {
            npc<Happy>("All aboard, then.")
            sail()
        }
    }

    private suspend fun Player.sail() {
        open("fade_out")
        delay(4)
        tele(3254, 3171)
        open("fade_in")
    }
}
