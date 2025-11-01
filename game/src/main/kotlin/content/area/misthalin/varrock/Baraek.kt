package content.area.misthalin.varrock

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.suspend.SuspendableContext

class Baraek : Script {

    init {
        npcOperate("Talk-to", "baraek") {
            if (holdsItem("bear_fur")) {
                choice {
                    option<Quiz>("Can you sell me some furs?") {
                        sellFur()
                    }
                    option<Neutral>("Hello. I am in search of a quest.") {
                        npc<Neutral>("Sorry kiddo, I'm a fur trader not a damsel in distress.")
                    }
                    option<Sad>("Would you like to buy my fur?") {
                        buyFur()
                    }
                }
            } else {
                choice {
                    option<Quiz>("Can you sell me some furs?") {
                        sellFur()
                    }
                    option<Neutral>("Hello. I am in search of a quest.") {
                        npc<Neutral>("Sorry kiddo, I'm a fur trader not a damsel in distress.")
                    }
                }
            }
        }
    }

    suspend fun Player.sellFur() {
        npc<Neutral>("Yeah, sure. They're 20 gold coins each.")
        choice {
            option<Neutral>("Yeah, OK, here you go.") {
                if (inventory.remove("coins", 20)) {
                    inventory.add("bear_fur")
                    item("bear_fur", 645, "Baraek sells you a fur.")
                } else {
                    player<Sad>("Oh dear, I don't have enough money!")
                    npc<Neutral>("Well, my best price is 18 coins.")
                    choice {
                        option<Neutral>("OK, here you go.") {
                            if (inventory.remove("coins", 18)) {
                                inventory.add("bear_fur")
                                item("bear_fur", 645, "Baraek sells you a fur.")
                            } else {
                                player<Sad>("Oh dear, I don't have that either.")
                                npc<Sad>("Well, I can't go any cheaper than that mate. I've got a family to feed.")
                                player<Sad>("Oh well, never mind.")
                            }
                        }
                        option<Neutral>("No thanks, I'll leave it.") {
                            npc<Neutral>("It's your loss mate.")
                        }
                    }
                }
            }
            option<Frustrated>("20 gold coins? That's an outrage!") {
                npc<Sad>("Well, I can't go any cheaper than that mate. I have a family to feed.")
            }
        }
    }

    suspend fun Player.buyFur() {
        npc<Neutral>("Let's have a look at it.")
        item("bear_fur", 645, "You hand Baraek your fur to look at.")
        // wait 4sec and cant move
        npc<Neutral>("It's not in the best condition. I guess I could give you 12 coins for it.")
        choice {
            option<Neutral>("Yeah, that'll do.") {
                inventory.remove("bear_fur", 1)
                inventory.add("coins", 12)
                player<Happy>("Thanks!")
            }
            option<Frustrated>("I think I'll keep hold of it actually!") {
                npc<Sad>("Oh ok. Didn't want it anyway!")
            }
        }
    }
}
