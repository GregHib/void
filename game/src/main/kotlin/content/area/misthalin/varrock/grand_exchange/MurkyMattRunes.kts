package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear

npcOperate("Talk-to", "murky_matt") {
    if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
        player<Talk>("Hello.")
        npc<Talk>("Arrrr, ${if (player.male) "me-lad" else "me-lady"}, I would speak with ye, but may I ask ye to speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance. Brugsen will give ye a great and plunderous lesson on the Grand Exchange and the Tutor a much shorter and simpler explanation.")
        return@npcOperate
    }
    player<Happy>("A pirate!")
    npc<Talk>("Arrrr, How'd ye be guessing that, ${if (player.male) "me-lad" else "me-lady"}?")
    player<Happy>("You're kidding, right?")
    npc<Talk>("Nay! Now, what is it ye be wantin? I can tell ye all about the prices of runes, I can.")
    menu()
}

suspend fun NPCOption<Player>.menu() {
    choice {
        option<Quiz>("What's a pirate doing here?") {
            npc<Talk>("By my sea-blistered skin, I could ask the same of you!")
            player<Quiz>("But... I'm not a pirate?")
            npc<Talk>("No? Then what's that smell? The smell o' someone spent too long at sea without a bath!")
            player<Talk>("I think that's probably you.")
            npc<Laugh>("Har har har!")
            npc<Happy>("We've got a stern landlubber 'ere! Well, let me tell ye, I'm here for the Grand Exchange! Gonna cash in me loot!")
            player<Quiz>("Don't you just want to sell it in a shop or trade it to someone specific?")
            npc<Talk>("By my wave-battered bones! Not when I can sell to the whole world from this very spot!")
            if (player.questCompleted("lunar_diplomacy")) {
                player<Talk>("You pirates are nothing but trouble! Why, once I travelled to Lunar Isle with a bunch of your type, and spent days sailing around in circles!")
                npc<Talk>("Then ye must know me brother! Murky Pat!")
                player<Talk>("Hmmm. Not so sure I remember him.")
                npc<Talk>("Well, 'e be on that ship for sure. And I remember 'im tellin' me about some guy like ye, getting all mixed up with curses and cabin boys.")
                player<Talk>("Yes! That was me!")
                npc<Talk>("Ye sure be a different character.")
            }
        }
        option<Talk>("Tell me about the prices of runes.") {
            player["common_item_costs"] = "runes"
            player.open("common_item_costs")
        }
        option<Happy>("I got to go, erm, swab some decks! Yarr!") {
            npc<Angry>("Defer your speech right there! Quit this derogatory and somewhat narrow-minded allusion that all folks of sea voyage are only concerned with washing the decks, looking after parrots and drinking rum. I'll have ye know there is much more to a pirate than meets the eye.")
            player<Talk>("Aye-aye, captain!")
            npc<Unamused>("...")
            player<Talk>("Oh, come on! Lighten up!")
        }
    }
}

npcOperate("Info-runes", "murky_matt") {
    if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
        player<Talk>("Hello.")
        npc<Talk>("Arrrr, ${if (player.male) "me-lad" else "me-lady"}, I would speak with ye, but may I ask ye to speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance. Brugsen will give ye a great and plunderous lesson on the Grand Exchange and the Tutor a much shorter and simpler explanation.")
        return@npcOperate
    }
    player["common_item_costs"] = "runes"
    player.open("common_item_costs")
}

npcOperate("Combine", "murky_matt") {
    if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
        player<Talk>("Hello.")
        npc<Talk>("Arrrr, ${if (player.male) "me-lad" else "me-lady"}, I must ask ye to get yerself some learnin' about the Grand Exchange first. Brugsen will give ye a great and plunderous lesson, or the Tutor can offer ye a much shorter and simpler explanation.")
        return@npcOperate
    }
    combineJewellery()
}

suspend fun NPCOption<Player>.combineJewellery() {
    player.inventory.transaction {
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
    when (player.inventory.transaction.error) {
        TransactionError.Invalid -> npc<Sad>("Arrr, I couldn't combine ye stuff.") // Custom message
        TransactionError.None -> npc<Happy>("Arr, all done.")
        else -> npc<Talk>("Arrr, ye've got nothing that I can combine.")
    }
}

val jewellery = mapOf(
    "games_necklace" to 8,
    "ring_of_duelling" to 8,
    "dig_site_pendant" to 5,
    "amulet_of_glory" to 4,
    "ring_of_slaying" to 8,
)
