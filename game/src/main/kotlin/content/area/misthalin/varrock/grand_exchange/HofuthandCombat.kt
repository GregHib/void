package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings

class HofuthandCombat : Script {

    init {
        npcOperate("Talk-to", "hofuthand") {
            if (Settings["grandExchange.tutorial.required", false] && !questCompleted("grand_exchange_tutorial")) {
                player<Talk>("Hello.")
                npc<Talk>("Hello? Ah you're new here, aren't you?")
                npc<Talk>("I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting, deeper lesson on the Grand Exchange and the Tutor will give a briefer, plain lesson.")
                return@npcOperate
            }
            player<Happy>("Hello!")
            npc<Uncertain>("What? Oh, hello. I was deep in thought. Did you want me to show you the prices of weapons and armour?")
            choice {
                flustered()
                showPrices()
                bye()
            }
        }

        npcOperate("Info-combat", "hofuthand") {
            if (Settings["grandExchange.tutorial.required", false] && !questCompleted("grand_exchange_tutorial")) {
                npc<Talk>("Huh? Oh, not till you've had training.")
                npc<Talk>("I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting, deeper lesson on the Grand Exchange and the Tutor will give a briefer, plain lesson.")
                return@npcOperate
            }
            set("common_item_costs", "combat")
            open("common_item_costs")
        }
    }

    fun ChoiceOption.showPrices() {
        option<Happy>("Yes, show me the prices of weapons and armour.") {
            set("common_item_costs", "combat")
            open("common_item_costs")
        }
    }

    fun ChoiceOption.bye() {
        option<Shifty>("I'll leave you alone.") {
            npc<Talk>("Thank you, I have much on my mind.")
        }
    }

    fun ChoiceOption.flustered() {
        option<Happy>("You seem a bit flustered.") {
            npc<Uncertain>("Sorry, I'm just deep in thought. I'm waiting for many deals to complete today.")
            player<Talk>("What sort of things are you selling?")
            npc<Uncertain>("Good old weapons and armour! My people - dwarves, you understand - are hoping I can trade with humans.")
            player<Happy>("It looks like you've come to the right place for that.")
            npc<Happy>("I have indeed, my friend. Now, can I help you?")
            choice {
                showPrices()
                bye()
            }
        }
    }
}
