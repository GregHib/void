package content.area.kandarin.yanille

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Leon : Script {

    val logger = InlineLogger()

    init {
        npcOperate("Talk-to", "leon") {
            item("hunters_crossbow", "Leon is gazing intently at the crossbow in his hands.")
            choice {
                place()
                infoCrossbow()
                infoAmmo()
                bye()
            }
        }

        npcOperate("Trade", "leon") {
            choice {
                option<Quiz>("Could I have a go with your crossbow?") {
                    buy()
                }
                makeAmmo()
                bye()
            }
        }
    }

    private fun ChoiceOption.place() = option<Quiz>("What is this place?") {
        npc<Neutral>("This is Aleck's Hunter Emporium. Basically, it's just a shop with a fancy name; you can buy various weapons and traps here.")
        choice {
            shop()
            infoCrossbow()
            infoAmmo()
            bye()
        }
    }

    private fun ChoiceOption.shop() = option<Quiz>("Can I buy some equipment from the shop then?") {
        npc<Confused>("Oh, this isn't my shop; the owner is Aleck over there behind the counter.")
        npc<Happy>("I experiment with weapon designs. I'm here because I've been trying to convince people to back my research and maybe sell some of my products - like this one I'm holding - in their shops.")
        npc<Sad>("Aleck doesn't seem to be interested, though.")
        choice {
            infoCrossbow()
            infoAmmo()
            bye()
        }
    }

    private fun ChoiceOption.infoCrossbow(): Unit = option<Quiz>("Can you tell me about your crossbow?") {
        npc<Happy>("It's good, isn't it? I designed it to incorporate the bones of various animals in its construction.")
        npc<Shifty>("It's a fair bit faster than an ordinary crossbow too; it'll take you far less time to reload between shots.")
        choice {
            crazy()
            trade()
            infoAmmo()
            bye()
        }
    }

    private fun ChoiceOption.crazy(): Unit = option<Laugh>("That's crazy, it'll never work!") {
        npc<Neutral>("That's what they said about my wind-powered mouse traps, too.")
        player<Quiz>("And did they work?")
        npc<Sad>("Well, they only ran into problems because people kept insisting on trying to use them indoors.")
        npc<Confused>("Anyway, I think my crossbow invention is showing a lot more promise.")
        choice {
            trade()
            infoAmmo()
            bye()
        }
    }

    private fun ChoiceOption.trade() {
        option<Happy>("That sounds good. Let's trade.") {
            buy()
        }
    }

    private suspend fun Player.buy() {
        npc<Sad>("I'm afraid with it being a prototype, I've only got a few for my own testing purposes.")
        npc<Happy>("Of course, for the right price, it might be worth my while to make another prototype.")
        player<Quiz>("How much?")
        npc<Neutral>("To you, 1300 gold pieces.")
        choice {
            option<Neutral>("Ok, here you go.") {
                inventory.transaction {
                    remove("coins", 1300)
                    add("hunters_crossbow")
                }
                when (inventory.transaction.error) {
                    is TransactionError.Deficient -> player<Sad>("Oh, actually I don't have enough money with me.")
                    is TransactionError.Full -> npc<Neutral>("Well, you look a little overburdened there at the moment. Maybe you should free up some space to carry it in first.")
                    TransactionError.None -> {
                        item("hunters_crossbow", "You hand over the money and the weapon designer presents you with a prototype crossbow.")
                        npc<Happy>("Here, you might as well take this one. Aleck over there doesn't seem interested and it'll save you a wait.")
                    }
                    else -> logger.warn { "Error buying hunters crossbow ${inventory.transaction.error}" }
                }
            }
            option<Neutral>("I'll give it a miss, thanks.") {
                npc<Neutral>("Your loss; they'll probably become highly valuable and sought after once I convince someone to market the things.")
            }
        }
    }

    private fun ChoiceOption.infoAmmo() = option<Happy>("Tell me about the ammo for your crossbow.") {
        npc<Confused>("Ah, I admit as a result of its... er... unique construction, it won't take just any old bolts.")
        npc<Happy>("If you can supply the materials and a token fee, I'd be happy to make some for you.")
        npc<Neutral>("I need kebbit spikes, lots of 'em. Not all kebbits have spikes, mind you. You'll be wanting prickly kebbits or, even better, razor-backed kebbits to get material hard enough.")
        choice {
            option<Quiz>("Can't I just make my own?") {
                npc<Neutral>("Yes, I suppose you could, although you'll need a steady hand with a knife and a chisel.")
                npc<Neutral>("The bolts have an unusual diameter, but basically you'll just need to be able to carve kebbit spikes into straight shafts.")
                npc<Quiz>("Meanwhile, since you're here, I can make some for you if you have the materials.")
                choice {
                    makeAmmo()
                    infoCrossbow()
                    bye()
                }
            }
            makeAmmo()
            infoCrossbow()
            bye()
        }
    }

    private fun ChoiceOption.bye() = option<Bored>("I'll be off now, excuse me.") {
        npc<Happy>("Well, if you ever find yourself in need of that innovative edge, you can always find me here.")
        player<Shifty>("... thanks.")
    }

    private fun ChoiceOption.makeAmmo() = option<Quiz>("Okay, can you make ammo for me?") {
        val (item, amount) = makeAmount(text = "How many batches would you like?", items = listOf("kebbit_bolts", "long_kebbit_bolts"), type = "Make", maximum = 27)
        when (item) {
            "kebbit_bolts" -> {
                inventory.transaction {
                    remove("kebbit_spike", amount)
                    remove("coins", 20 * amount)
                    add("kebbit_bolts", 12 * amount)
                }
                when (inventory.transaction.error) {
                    is TransactionError.Deficient -> items("kebbit_spike", "coins_4", "You need $amount kebbit ${"spike".plural(amount)} and ${amount * 20} coins to make ${12 * amount} kebbit bolts.")
                    TransactionError.None -> item("kebbit_bolts", "You hand the weapon designer $amount ${"spike".plural(amount)} and ${amount * 20} coins. In return he presents you with ${12 * amount} bolts.")
                    else -> logger.warn { "Error buying kebbit bolts ${inventory.transaction.error}." }
                }
            }
            "long_kebbit_bolts" -> {
                inventory.transaction {
                    remove("long_kebbit_spike", amount)
                    remove("coins", 40 * amount)
                    add("long_kebbit_bolts", 12 * amount)
                }
                when (inventory.transaction.error) {
                    is TransactionError.Deficient -> items("long_kebbit_spike", "coins_4", "You need $amount long kebbit ${"spike".plural(amount)} and ${amount * 40} coins to make ${12 * amount} long kebbit bolts.")
                    TransactionError.None -> item("long_kebbit_bolts", "You hand the weapon designer $amount long ${"spike".plural(amount)} and ${amount * 40} coins. In return he presents you with ${12 * amount} long bolts.")
                    else -> logger.warn { "Error buying long kebbit bolts ${inventory.transaction.error}." }
                }
            }
            else -> logger.warn { "Error making kebbit bolts." }
        }
    }
}
