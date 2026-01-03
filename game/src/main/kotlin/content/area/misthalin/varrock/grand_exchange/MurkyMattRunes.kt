package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear

class MurkyMattRunes : Script {

    val jewellery = mapOf(
        "games_necklace" to 8,
        "ring_of_duelling" to 8,
        "dig_site_pendant" to 5,
        "amulet_of_glory" to 4,
        "ring_of_slaying" to 8,
    )

    init {
        npcOperate("Talk-to", "murky_matt") {
            if (Settings["grandExchange.tutorial.required", false] && !questCompleted("grand_exchange_tutorial")) {
                player<Neutral>("Hello.")
                npc<Neutral>("Arrrr, ${if (male) "me-lad" else "me-lady"}, I would speak with ye, but may I ask ye to speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance. Brugsen will give ye a great and plunderous lesson on the Grand Exchange and the Tutor a much shorter and simpler explanation.")
                return@npcOperate
            }
            player<Happy>("A pirate!")
            npc<Neutral>("Arrrr, How'd ye be guessing that, ${if (male) "me-lad" else "me-lady"}?")
            player<Happy>("You're kidding, right?")
            npc<Neutral>("Nay! Now, what is it ye be wantin? I can tell ye all about the prices of runes, I can.")
            menu()
        }

        npcOperate("Info-runes", "murky_matt") {
            if (Settings["grandExchange.tutorial.required", false] && !questCompleted("grand_exchange_tutorial")) {
                player<Neutral>("Hello.")
                npc<Neutral>("Arrrr, ${if (male) "me-lad" else "me-lady"}, I would speak with ye, but may I ask ye to speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance. Brugsen will give ye a great and plunderous lesson on the Grand Exchange and the Tutor a much shorter and simpler explanation.")
                return@npcOperate
            }
            set("common_item_costs", "runes")
            open("common_item_costs")
        }

        npcOperate("Combine", "murky_matt") {
            if (Settings["grandExchange.tutorial.required", false] && !questCompleted("grand_exchange_tutorial")) {
                player<Neutral>("Hello.")
                npc<Neutral>("Arrrr, ${if (male) "me-lad" else "me-lady"}, I must ask ye to get yerself some learnin' about the Grand Exchange first. Brugsen will give ye a great and plunderous lesson, or the Tutor can offer ye a much shorter and simpler explanation.")
                return@npcOperate
            }
            combineJewellery()
        }
    }

    suspend fun Player.menu() {
        choice {
            option<Quiz>("What's a pirate doing here?") {
                npc<Neutral>("By my sea-blistered skin, I could ask the same of you!")
                player<Quiz>("But... I'm not a pirate?")
                npc<Neutral>("No? Then what's that smell? The smell o' someone spent too long at sea without a bath!")
                player<Neutral>("I think that's probably you.")
                npc<Cackle>("Har har har!")
                npc<Happy>("We've got a stern landlubber 'ere! Well, let me tell ye, I'm here for the Grand Exchange! Gonna cash in me loot!")
                player<Quiz>("Don't you just want to sell it in a shop or trade it to someone specific?")
                npc<Neutral>("By my wave-battered bones! Not when I can sell to the whole world from this very spot!")
                if (questCompleted("lunar_diplomacy")) {
                    player<Neutral>("You pirates are nothing but trouble! Why, once I travelled to Lunar Isle with a bunch of your type, and spent days sailing around in circles!")
                    npc<Neutral>("Then ye must know me brother! Murky Pat!")
                    player<Neutral>("Hmmm. Not so sure I remember him.")
                    npc<Neutral>("Well, 'e be on that ship for sure. And I remember 'im tellin' me about some guy like ye, getting all mixed up with curses and cabin boys.")
                    player<Neutral>("Yes! That was me!")
                    npc<Neutral>("Ye sure be a different character.")
                }
            }
            option<Neutral>("Tell me about the prices of runes.") {
                set("common_item_costs", "runes")
                open("common_item_costs")
            }
            option<Happy>("I got to go, erm, swab some decks! Yarr!") {
                npc<Angry>("Defer your speech right there! Quit this derogatory and somewhat narrow-minded allusion that all folks of sea voyage are only concerned with washing the decks, looking after parrots and drinking rum. I'll have ye know there is much more to a pirate than meets the eye.")
                player<Neutral>("Aye-aye, captain!")
                npc<Unamused>("...")
                player<Neutral>("Oh, come on! Lighten up!")
            }
        }
    }

    suspend fun Player.combineJewellery() {
        inventory.transaction {
            val chargeMap = mutableMapOf<String, Int>()
            for (index in inventory.items.indices) {
                val item = inventory.items[index]
                val type = item.id.substringBeforeLast("_")
                if (jewellery.containsKey(type)) {
                    clear(index)
                    val doses = item.id.substringAfterLast("_").toInt()
                    chargeMap[type] = chargeMap.getOrDefault(type, 0) + doses
                }
            }
            for ((type, doses) in chargeMap) {
                val denominator = jewellery[type]!!
                if (doses >= denominator) {
                    add("${type}_$denominator", doses / denominator)
                }
                val remaining = doses.rem(denominator)
                if (remaining > 0) {
                    add("${type}_$remaining")
                }
            }
        }
        when (inventory.transaction.error) {
            TransactionError.Invalid -> npc<Disheartened>("Arrr, I couldn't combine ye stuff.") // Custom message
            TransactionError.None -> npc<Happy>("Arr, all done.")
            else -> npc<Neutral>("Arrr, ye've got nothing that I can combine.")
        }
    }
}
