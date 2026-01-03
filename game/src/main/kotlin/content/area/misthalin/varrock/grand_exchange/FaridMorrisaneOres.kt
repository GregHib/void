package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.*
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings

class FaridMorrisaneOres : Script {

    init {
        npcOperate("Talk-to", "farid_morrisane") {
            if (Settings["grandExchange.tutorial.required", false] && !questCompleted("grand_exchange_tutorial")) {
                player<Neutral>("Hello.")
                npc<Neutral>("Hmmm, a new trader. I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting and complete lesson and the Tutor will give a smaller, plain lesson.")
                return@npcOperate
            }
            player<Happy>("Hello, little boy.")
            npc<Neutral>("I would prefer it if you didn't speak to me in such a manner. I'll have you know I'm an accomplished merchant.")
            choice {
                calmDown()
                oresAndBars()
                bye()
            }
        }

        npcOperate("Info-ores", "farid_morrisane") {
            if (Settings["grandExchange.tutorial.required", false] && !questCompleted("grand_exchange_tutorial")) {
                npc<Neutral>("Hmmm, a new trader. I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting and complete lesson and the Tutor will give a smaller, plain lesson.")
                return@npcOperate
            }
            set("common_item_costs", "ores")
            open("common_item_costs")
        }
    }

    fun ChoiceOption.oresAndBars() {
        option<Quiz>("Can you show me the prices of ores and bars?") {
            set("common_item_costs", "ores")
            open("common_item_costs")
        }
    }

    fun ChoiceOption.bye() {
        option<Neutral>("I best go and speak with someone more my height.") {
            npc<Neutral>("Then I shall not stop you. I've too much work to do.")
        }
    }

    fun ChoiceOption.calmDown() {
        option<Neutral>("Calm down, junior.") {
            npc<Neutral>("Don't tell me to calm down! And don't call me 'junior'.")
            npc<Neutral>("I'll have you know I am Farid Morrisane, son of Ali Morrisane, the world's greatest merchant!")
            player<Quiz>("Then why are you here and not him?")
            npc<Neutral>("My dad has given me the responsibility of expanding our business here.")
            player<Quiz>("And you're up to the task? What a grown up boy you are! Mummy and daddy must be very pleased!")
            npc<Neutral>("Look, mate - I may be young, I may be short, but I'm a respected merchant around here and don't have time to deal with simpletons like you.")
            choice {
                oresAndBars()
                bye()
            }
        }
    }
}
