package content.area.misthalin.lumbridge.swamp

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class CandleSeller : Script {
    init {
        npcOperate("Talk-to", "candle_seller") {
            npc<Neutral>("Do you want a lit candle for 1000 gold?")
            choice {
                option<Neutral>("Yes please.") {
                    inventory.transaction {
                        remove("coins", 1000)
                        add("candle_lit")
                    }
                    when (inventory.transaction.error) {
                        is TransactionError.Deficient -> {
                            player<Sad>("But I don't have that kind of money on me.")
                            npc<Angry>("Well then, no candle for you!")
                        }
                        is TransactionError.Full -> if (inventory.remove("coins", 1000)) {
                            addOrDrop("candle_lit")
                            warn()
                        }
                        TransactionError.None -> warn()
                        else -> {}
                    }
                }
                option<Shock>("One thousand gold?!") {
                    npc<Neutral>("Look, you're not going to be able to survive down that hole without a light source.")
                    npc<Neutral>("So you could go off to the candle shop to buy one more cheaply. You could even make your own lantern, which is a lot better.")
                    npc<Neutral>("But I bet you want to find out what's down there right now, don't you? And you can pay me 1000 gold for the privilege!")
                    set("candle_seller_lantern_dialogue", true)
                    choice {
                        option<Neutral>("All right, you win, I'll buy a candle.") {
                            player<Sad>("But I don't have that kind of money on me.")
                            npc<Angry>("Well then, no candle for you!")
                        }
                        option<Angry>("No way.")
                        howToMakeLanterns()
                    }
                }
                option<Neutral>("No thanks, I'd rather curse the darkness.")
                if (get("candle_seller_lantern_dialogue", false)) {
                    howToMakeLanterns()
                }
            }
        }
    }

    private suspend fun Player.warn() {
        npc<Happy>("Here you go then.")
        npc<Neutral>("I should warn you, though, it can be dangerous to take a naked flame down there. You'd be better off making a lantern.")
        choice {
            option<Quiz>("What's so dangerous about a naked flame?") {
                npc<Shifty>("Heh heh... You'll find out.")
            }
            howToMakeLanterns()
            option<Neutral>("Thanks, bye.")
        }
    }

    private fun ChoiceOption.howToMakeLanterns() {
        option<Quiz>("How do you make lanterns?") {
            npc<Neutral>("Out of glass. The more advanced lanterns have a metal component as well.")
            npc<Neutral>("Firstly you can make a simple candle lantern out of glass. It's just like a candle, but the flame isn't exposed, so it's safer.")
            npc<Neutral>("Then you can make an oil lamp, which is brighter but has an exposed flame. But if you make an iron frame for it you can turn it into an oil lantern.")
            npc<Neutral>("Finally there's the bullseye lantern. You'll need to make a frame out of steel and add a glass lens.")
            npc<Neutral>("Once you've made your lamp or lantern, you'll need to make lamp oil for it. The chemist near Rimmington has a machine for that.")
            npc<Neutral>("For any light source, you'll need a tinderbox to light it. Keep your tinderbox handy in case it goes out!")
            npc<Happy>("But if all that's too complicated, you can buy a candle right here for 1000 gold!")
            choice {
                option<Neutral>("All right, you win, I'll buy a candle.")
                option<Neutral>("No thanks, I'd rather curse the darkness.")
            }
        }
    }
}
