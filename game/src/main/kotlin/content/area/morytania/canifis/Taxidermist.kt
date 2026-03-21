package content.area.morytania.canifis

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.entity.Spawn.Companion.player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class Taxidermist : Script {
    init {
        npcOperate("Talk-to", "taxidermist") { (target) ->
            npc<Neutral>("Oh, hello. Have you got something you want preserving?")
            choice {
                option("Yes please") {
                    player<Happy>("Yes please.")
                    npc<Neutral>("Give it to me to look at then.")
                }
                option("Not right now") {
                    player<Neutral>("Not right now.")
                    npc<Neutral>("Well, you go kill things so I can stuff them, eh?")
                }
                option<Quiz>("What?") {
                    npc<Neutral>("If you bring me a monster head or a very big fish, I can preserve it for you so you can mount it in your house.")
                    npc<Neutral>("I hear there are all sorts of exotic creatures in the Slayer Tower -- I'd like a chance to stuff one of them!")
                }
            }
        }

        itemOnNPCOperate(npc = "taxidermist") {
            var price = 0
            when (it.item.id) {
                "raw_chicken" -> {
                    npc<Neutral>("Killing a chicken is hardly worth boasting about!")
                    return@itemOnNPCOperate
                }
                "raw_cavefish", "raw_rocktail", "raw_monkfish", "raw_karambwanji", "leaping_trout", "leaping_salmon", "leaping_sturgeon", "raw_shrimps", "raw_anchovies", "raw_sardine", "raw_salmon", "raw_trout", "raw_cod", "raw_herring", "raw_pike", "raw_mackerel", "raw_tuna", "raw_bass", "raw_swordfish", "raw_lobster", "raw_shark", "raw_manta_ray", "raw_sea_turtle", "raw_karambwan", "raw_rainbow_fish", "raw_crayfish" -> {
                    npc<Neutral>("That's a pretty ordinary fish, isn't it? Not really worth preserving.")
                    player<Sad>("You should have seen the one that got away!")
                    return@itemOnNPCOperate
                }
                "big_bass" -> {
                    npc<Shock>("That's a mighty fine sea bass you've caught there.")
                    price = 1_000
                }
                "big_swordfish" -> {
                    npc<Shock>("Don't point that thing at me!")
                    price = 2_500
                }
                "big_shark" -> {
                    npc<Shock>("That's quite a fearsome shark! You've done everyone a service by removing it from the sea!")
                    price = 5_000
                }
                "crawling_hand" -> {
                    npc<Shock>("That's a very fine crawling hand.")
                    price = 1_000
                }
                "cockatrice_head" -> {
                    npc<Shock>("A cockatrice! Beautiful, isn't it? Look at the plumage!")
                    price = 2_000
                }
                "basilisk_head" -> {
                    npc<Shock>("My, he's a scary-looking fellow, isn't he? He'll look good on your wall!")
                    price = 4_000
                }
                "kurask_head" -> {
                    npc<Shock>("A kurask? Splendid! Look at those horns!")
                    price = 6_000
                }
                "abyssal_head" -> {
                    npc<Shock>("Goodness, an abyssal demon!")
                    npc<Quiz>("See how it's still glowing? I'll have to use some magic to preserve that.")
                    price = 12_000
                }
                "king_black_dragon_head" -> {
                    npc<Shock>("Three?! This must be a King Black Dragon!")
                    npc<Shock>("I'll have to get out my heavy duty tools -- this skin's as tough as iron!")
                    price = 50_000
                }
                "kalphite_queen_head" -> {
                    npc<Shock>("That must be the biggest kalphite I've ever seen!")
                    npc<Confused>("Preserving insects is always tricky. I'll have to be careful...")
                    price = 50_000
                }
                else -> {
                    npc<Laugh>("Don't be silly, I can't preserve that!")
                    return@itemOnNPCOperate
                }
            }
            npc<Neutral>("I can preserve that for you for ${price.toDigitGroupString()} coins.")
            if (inventory.count("coins") < price) {
                player<Sad>("Maybe another time.")
                return@itemOnNPCOperate
            }
            choice {
                option<Neutral>("Yes please.") {
                    inventory.transaction {
                        remove("coins", price)
                        replace(it.item.id, "${it.item.id}_stuffed")
                    }
                    if (inventory.transaction.error != TransactionError.None) {
                        npc<Happy>("There you go!")
                    }
                }
                option<Neutral>("No thanks.") {
                    npc<Neutral>("All right, come back if you change your mind, eh?")
                }
            }
        }
    }
}
