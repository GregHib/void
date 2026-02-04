package content.area.misthalin.varrock

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class Benny : Script {
    private val overheads = listOf(
        "Read all about it!",
        "Extra! Extra! Read all about it!",
        "Buy your Varrock Herald now!",
        "Varrock Herald, now only 50 coins!",
        "Get your Varrock Herald here!",
        "Varrock Herald, on sale here!",
    )

    init {
        npcSpawn("benny") {
            softTimers.start("benny_overhead")
        }
        npcTimerStart("benny_overhead") {
            TimeUnit.SECONDS.toTicks(15)
        }
        npcTimerTick("benny_overhead") {
            say(overheads.random())
            Timer.CONTINUE
        }
        npcOperate("Talk-to", "benny") {
            suspend fun purchase() {
                choice {
                    option<Neutral>("Sure, here you go...") {
                        inventory.transaction {
                            remove("coins", 50)
                            add("newspaper")
                        }
                        when (inventory.transaction.error) {
                            is TransactionError.Full -> {
                                inventoryFull()
                                npc<Shifty>("Sorry, you don't have enough space in your inventory.")
                            }
                            TransactionError.None -> {
                                npc<Happy>("There you go. Pleasure doing business with you!")
                            }
                            else -> npc<Shifty>("Sorry, you don't have enough coins. It costs 50 coins.")
                        }
                    }
                    option<Neutral>("No thank you.") {
                        npc<Happy>("Well, no cash, no paper. Live in ignorance.")
                    }
                }
            }

            choice {
                option<Quiz>("Can I have a newspaper, please?") {
                    npc<Happy>("Certainly, Guv. That'll be 50 coins, please.")
                    purchase()
                }
                option<Quiz>("How much does a paper cost?") {
                    npc<Happy>("Just 50 coins! An absolute bargain! Want one?")
                    purchase()
                }
                option<Quiz>("Varrock Herald? Never heard of it.") {
                    npc<Neutral>(
                        "For the illiterate amongst us, I shall elucidate. The Varrock Herald is a new newspaper. " +
                            "It is edited, printed and published by myself, Benny Gutenberg, and each edition promises " +
                            "to enthrall the reader with captivating material!",
                    )
                    npc<Happy>("Now, can I interest you in buying one for a mere 50 coins?")
                    purchase()
                }
                option<Quiz>("Anything interesting in there?") {
                    npc<Happy>(
                        "Of course there is, mate. Packed full of thought provoking insights, " +
                            "contentious interviews and celebrity scandalmongering! An excellent read " +
                            "and all for just 50 coins! Want one?",
                    )
                    purchase()
                }
            }
        }
        npcOperate("Trade", "benny") {
            openShop("varrock_herald")
        }
    }
}
