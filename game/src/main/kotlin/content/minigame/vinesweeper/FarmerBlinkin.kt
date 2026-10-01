package content.minigame.vinesweeper

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class FarmerBlinkin : Script {

    init {
        npcOperate("Talk-to", "farmer_blinkin_minesweeper") {
            npc<Happy>("Ah, a visitor! Fancy helping me find my seeds? My farmers have planted them all over the field and forgotten where.")
            menu()
        }

        npcOperate("Buy-flags", "farmer_blinkin_minesweeper") {
            buyFlags()
        }

        npcOperate("Buy-roots", "farmer_blinkin_minesweeper") {
            buyOgleroots()
        }
    }

    private suspend fun Player.menu() {
        choice {
            option<Quiz>("How do I play?") {
                open("vinesweeper_instructions")
            }
            option<Quiz>("Can I have some flags?") {
                buyFlags()
            }
            option<Quiz>("Can I buy some ogleroots?") {
                buyOgleroots()
            }
            option<Neutral>("Not right now.") {
                player<Neutral>("Not right now.")
            }
        }
    }
}
