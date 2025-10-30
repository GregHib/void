package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceBuilder
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear

class BobBarterHerbs : Script {

    val potions = setOf(
        "attack_potion",
        "strength_potion",
        "defence_potion",
        "prayer_potion",
        "super_attack",
        "super_strength",
        "super_defence",
        "super_ranging_potion",
        "fishing_potion",
        "antipoison",
        "super_antipoison",
        "zamorak_brew",
        "antifire",
        "energy_potion",
        "super_energy",
        "super_restore",
        "agility_potion",
        "super_magic_potion",
        "serum_207",
        "serum_208",
        "olive_oil",
        "sacred_oil",
        "guthix_rest",
        "relicyms_balm",
        "antipoison+",
        "antipoison++",
        "saradomin_brew",
        "guthix_balance",
        "sanfew_serum",
        "crafting_potion",
        "fletching_potion",
        "hunter_potion",
        "combat_potion",
        "recover_special",
        "super_antifire",
        "extreme_attack",
        "extreme_strength",
        "extreme_defence",
        "extreme_magic",
        "extreme_ranging",
        "super_prayer",
        "overload",
    )

    init {
        npcOperate("Talk-to", "bob_barter") {
            if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
                player<Talk>("Hello.")
                npc<Talk>("Mate, I haven't got time for you yet. I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting lesson on the Grand Exchange and the Tutor will give a simpler, plain lesson.")
                return@npcOperate
            }
            npc<Happy>("Hello, chum, fancy buyin' some designer jewellery? They've come all the way from Ardougne! Most pukka!")
            player<Shifty>("Erm, no. I'm all set, thanks.")
            npc<Happy>("Okay, chum, would you like to show you the very latest potion prices?")
            choice {
                option<Talk>("Who are you?") {
                    npc<Happy>("Why, I'm Bob! Your friendly seller of smashin' goods!")
                    player<Quiz>("So what do you have to sell?")
                    npc<Talk>("Oh, not much at the moment. Cuz, ya know, business being so well and cushie.")
                    player<Talk>("You don't really look like you're being so successful.")
                    npc<Talk>("You plonka! It's all a show, innit! If I let people knows I'm in good business they'll want a share of the moolah!")
                    player<Talk>("You conveniently have a response for everything.")
                    npc<Chuckle>("That's the Ardougne way, my friend.")
                    choice {
                        showPrices()
                        option<Shifty>("I'll leave you to it.")
                    }
                }
                showPrices()
                option<Talk>("I'll leave you too it")
            }
        }

        npcOperate("Info-herbs", "bob_barter") {
            if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
                npc<Talk>("You'll need a tiny bit of training first, chum. I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting lesson on the Grand Exchange and the Tutor will give a smaller, plain lesson.")
                return@npcOperate
            }
            player["common_item_costs"] = "herbs"
            player.open("common_item_costs")
        }

        npcOperate("Decant", "bob_barter") {
            if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
                player<Talk>("Hello.")
                npc<Talk>("Mate, I haven't got time for you yet. I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting lesson on the Grand Exchange and the Tutor will give a simpler, plain lesson.")
                return@npcOperate
            }
            decantPotions()
        }
    }

    fun ChoiceBuilder<NPCOption<Player>>.showPrices() {
        option<Talk>("Can you show me the prices for herbs?") {
            player["common_item_costs"] = "herbs"
            player.open("common_item_costs")
        }
    }

    suspend fun NPCOption<Player>.decantPotions() {
        // Check if the player has any potions to decant
        player.inventory.transaction {
            val potionMap = mutableMapOf<String, Int>()
            for (index in inventory.items.indices) {
                val item = inventory.items[index]
                val type = item.id.substringBeforeLast("_")
                if (potions.contains(type)) {
                    clear(index)
                    val doses = item.id.substringAfterLast("_").toInt()
                    potionMap[type] = potionMap.getOrDefault(type, 0) + doses
                }
            }
            for ((type, doses) in potionMap) {
                if (doses >= 4) {
                    add("${type}_4", doses / 4)
                }
                val remaining = doses.rem(4)
                if (remaining > 0) {
                    add("${type}_$remaining")
                }
            }
        }
        when (player.inventory.transaction.error) {
            TransactionError.Invalid -> npc<Sad>("I wasn't able to decant your potions.")
            TransactionError.None -> npc<Happy>("There you go, chum.")
            else -> npc<Uncertain>("Sorry, I can't do anything with those potions.")
        }
    }
}
