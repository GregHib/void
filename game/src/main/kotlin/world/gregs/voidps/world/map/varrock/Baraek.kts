package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "baraek") {
    if (player.holdsItem("bear_fur")) {
        choice {
            option<Unsure>("Can you sell me some furs?") {
                sellFur()
            }
            option<Talking>("Hello. I am in search of a quest.") {
                npc<Talking>("Sorry kiddo, I'm a fur trader not a damsel in distress.")
            }
            option<Sad>("Would you like to buy my fur?") {
                buyFur()
            }
        }
    } else {
        choice {
            option<Unsure>("Can you sell me some furs?") {
                sellFur()
            }
            option<Talking>("Hello. I am in search of a quest.") {
                npc<Talking>("Sorry kiddo, I'm a fur trader not a damsel in distress.")
            }
        }
    }
}


suspend fun CharacterContext.sellFur() {
    npc<Talking>("Yeah, sure. They're 20 gold coins each.")
    choice {
        option<Talking>("Yeah, OK, here you go.") {
            if (player.inventory.remove("coins", 20)) {
                player.inventory.add("bear_fur")
                item("bear_fur", 645, "Baraek sells you a fur.")
            } else {
                player<Sad>("Oh dear, I don't have enough money!")
                npc<Talking>("Well, my best price is 18 coins.")
                choice {
                    option<Talking>("OK, here you go.") {
                        if (player.inventory.remove("coins", 18)) {
                            player.inventory.add("bear_fur")
                            item("bear_fur", 645, "Baraek sells you a fur.")
                        } else {
                            player<Sad>("Oh dear, I don't have that either.")
                            npc<Sad>("Well, I can't go any cheaper than that mate. I've got a family to feed.")
                            player<Sad>("Oh well, never mind.")
                        }
                    }
                    option<Talking>("No thanks, I'll leave it.") {
                        npc<Talking>("It's your loss mate.")
                    }
                }
            }
        }
        option<Angry>("20 gold coins? That's an outrage!") {
            npc<Sad>("Well, I can't go any cheaper than that mate. I have a family to feed.")
        }
    }
}

suspend fun CharacterContext.buyFur() {
    npc<Talking>("Let's have a look at it.")
    item("bear_fur", 645, "You hand Baraek your fur to look at.")
    //wait 4sec and cant move
    npc<Talking>("It's not in the best condition. I guess I could give you 12 coins for it.")
    choice {
        option<Talking>("Yeah, that'll do.") {
            player.inventory.remove("bear_fur", 1)
            player.inventory.add("coins", 12)
            player<Cheerful>("Thanks!")
        }
        option<Angry>("I think I'll keep hold of it actually!") {
            npc<Sad>("Oh ok. Didn't want it anyway!")
        }
    }
}