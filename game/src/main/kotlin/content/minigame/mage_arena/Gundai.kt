package content.minigame.mage_arena

import content.entity.player.bank.pin.openBank
import content.entity.player.bank.pin.openCollection
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open

class Gundai : Script {

    init {
        npcOperate("Talk-to", "gundai") {
            player<Quiz>("Hello, what are you doing out here?")
            npc<Neutral>("I'm a banker, the only one around these dangerous parts.")
            choice {
                option<Happy>("Cool, I'd like to access my bank account please.") {
                    openBank()
                }
                option<Quiz>("Right, so can I check my PIN settings?") {
                    open("bank_pin_settings")
                }
                option<Neutral>("I'd like to collect items.") {
                    openCollection()
                }
                option<Neutral>("Well, now I know.") {
                    npc<Neutral>("Knowledge is power my friend.")
                }
            }
        }
        npcOperate("Bank", "gundai") {
            openBank()
        }
        npcOperate("Collect", "gundai") {
            openCollection()
        }
    }
}
