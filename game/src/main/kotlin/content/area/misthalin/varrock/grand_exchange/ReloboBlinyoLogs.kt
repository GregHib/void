package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.RollEyes
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
@Script
class ReloboBlinyoLogs {

    init {
        npcOperate("Talk-to", "relobo_blinyo") {
            if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
                player<Talk>("You look rather out of place...")
                npc<Talk>("That I may do, but so do you! I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting lesson on the Grand Exchange and the Tutor will give a smaller, plain lesson.")
                return@npcOperate
            }
            player<Talk>("Hey there.")
            npc<Talk>("Why, hell to you too, my friend.")
            menu()
        }

        npcOperate("Info-logs", "relobo_blinyo") {
            if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
                npc<Talk>("Not so fast! I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting lesson on the Grand Exchange and the Tutor will give a smaller, plain lesson.")
                return@npcOperate
            }
            player["common_item_costs"] = "logs"
            player.open("common_item_costs")
        }

    }

    suspend fun NPCOption<Player>.menu() {
        choice {
            option<Talk>("You look like you've travelled a fair distance.") {
                npc<Quiz>("What gave me away?")
                player<Talk>("I don't mean to be rude, but the face paint and hair, for starters.")
                npc<Talk>("Ah, yes. I'm from Shilo Village on Karamja, it's a style I've had since I was little.")
                player<Quiz>("Then tell me, why are you so far from home?")
                npc<Happy>("This Grand Exchange! Isn't it marvellous! I've never seen anything like it in my life. My people were not pleased to see me break traditions to visit such a place, but I hope to make some serious profit. Then they'll see I was right!")
                player<Talk>("So, what are you selling?")
                npc<Talk>("Logs! Of all kinds! That's my plan, at least. Nature is one thing my people understand very well.")
                menu()
            }
            option<Talk>("I'm trying to find the prices for logs.") {
                npc<Talk>("Then you've come to the right person.")
                player["common_item_costs"] = "logs"
                player.open("common_item_costs")
            }
            option<RollEyes>("Sorry, I need to make tracks.") {
                npc<Talk>("Okay, nice talking to you!")
            }
        }
    }
}
