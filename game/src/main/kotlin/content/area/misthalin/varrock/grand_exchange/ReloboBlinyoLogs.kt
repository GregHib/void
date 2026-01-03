package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player

class ReloboBlinyoLogs : Script {

    init {
        npcOperate("Talk-to", "relobo_blinyo") {
            if (Settings["grandExchange.tutorial.required", false] && !questCompleted("grand_exchange_tutorial")) {
                player<Neutral>("You look rather out of place...")
                npc<Neutral>("That I may do, but so do you! I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting lesson on the Grand Exchange and the Tutor will give a smaller, plain lesson.")
                return@npcOperate
            }
            player<Neutral>("Hey there.")
            npc<Neutral>("Why, hell to you too, my friend.")
            menu()
        }

        npcOperate("Info-logs", "relobo_blinyo") {
            if (Settings["grandExchange.tutorial.required", false] && !questCompleted("grand_exchange_tutorial")) {
                npc<Neutral>("Not so fast! I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting lesson on the Grand Exchange and the Tutor will give a smaller, plain lesson.")
                return@npcOperate
            }
            set("common_item_costs", "logs")
            open("common_item_costs")
        }
    }

    suspend fun Player.menu() {
        choice {
            option<Neutral>("You look like you've travelled a fair distance.") {
                npc<Quiz>("What gave me away?")
                player<Neutral>("I don't mean to be rude, but the face paint and hair, for starters.")
                npc<Neutral>("Ah, yes. I'm from Shilo Village on Karamja, it's a style I've had since I was little.")
                player<Quiz>("Then tell me, why are you so far from home?")
                npc<Happy>("This Grand Exchange! Isn't it marvellous! I've never seen anything like it in my life. My people were not pleased to see me break traditions to visit such a place, but I hope to make some serious profit. Then they'll see I was right!")
                player<Neutral>("So, what are you selling?")
                npc<Neutral>("Logs! Of all kinds! That's my plan, at least. Nature is one thing my people understand very well.")
                menu()
            }
            option<Neutral>("I'm trying to find the prices for logs.") {
                npc<Neutral>("Then you've come to the right person.")
                set("common_item_costs", "logs")
                open("common_item_costs")
            }
            option<Bored>("Sorry, I need to make tracks.") {
                npc<Neutral>("Okay, nice talking to you!")
            }
        }
    }
}
