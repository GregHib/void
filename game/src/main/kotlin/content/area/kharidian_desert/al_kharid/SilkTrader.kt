package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class SilkTrader : Script {

    init {
        npcOperate("Talk-to", "silk_trader") {
            dialogue()
        }

        npcOperate("Trade", "silk_trader") {
            dialogue()
        }
    }

    suspend fun Player.dialogue() {
        npc<Neutral>("Do you want to buy any fine silks?")
        choice {
            option<Quiz>("How much are they?") {
                npc<Neutral>("3 gp.")
                choice {
                    option<Shock>("No. That's too much for me.") {
                        npc<Quiz>("2 gp and that's as low as I'll go.")
                        npc<Sad>("I'm not selling it for any less. You'll probably go and sell it in Varrock for a profit, anyway.")
                        choice {
                            option<Neutral>("2 gp sounds good.") {
                                buySilk(2)
                            }
                            option<Neutral>("No, really. I don't want it.") {
                                npc<Neutral>("Okay, but that's the best price you're going to get.")
                            }
                        }
                    }
                    option<Neutral>("Okay, that sounds good.") {
                        buySilk(3)
                    }
                    option<Neutral>("No. Silk doesn't suit me.")
                }
            }
            option<Neutral>("No. Silk doesn't suit me.")
        }
    }

    private suspend fun Player.buySilk(price: Int) {
        if (!inventory.contains("coins", price)) {
            player<Sad>("Oh dear. I don't have enough money.")
            npc<Neutral>("Well, come back when you do have some money!")
            return
        }
        if (!inventory.add("silk")) {
            player<Sad>("I don't have enough room, sorry.")
            return
        }
        inventory.remove("coins", price)
        item("silk", "You buy some silk for $price gp.")
    }
}
