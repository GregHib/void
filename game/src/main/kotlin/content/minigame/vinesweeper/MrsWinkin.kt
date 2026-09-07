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

class MrsWinkin : Script {

    init {
        npcOperate("Talk-to", "mrs__winkin_minesweeper") {
            npc<Happy>("Hello there, dearie. Welcome to Winkin's Farm. What can I do for you?")
            menu()
        }

        npcOperate("Trade", "mrs__winkin_minesweeper") {
            open("vinesweeper_rewards")
        }

        npcOperate("Buy flags", "mrs__winkin_minesweeper") {
            buyFlags()
        }
    }

    private suspend fun Player.menu() {
        choice {
            option<Quiz>("What is this place?") {
                npc<Happy>("This is our farm. My husband's gnome farmers plant seeds all over the field, but the silly things never remember where they put them!")
                npc<Neutral>("If you can work out where the seeds are and mark them with flags, the farmers will dig them up and reward you with points. I'll swap those points for seeds or Farming experience.")
                npc<Neutral>("Have a read of the instruction signs around the farm, or ask Farmer Blinkin outside if you get stuck.")
                menu()
            }
            option<Quiz>("I'd like to see your rewards.") {
                open("vinesweeper_rewards")
            }
            option<Quiz>("Can I have some flags?") {
                buyFlags()
            }
            option<Quiz>("Can I buy a spade?") {
                buySpade()
            }
            option<Neutral>("Nothing, thanks.") {
                player<Neutral>("Nothing, thanks.")
            }
        }
    }
}
