package content.area.misthalin.varrock.blue_moon_inn

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

// Source: https://runescape.wiki/w/Transcript:Cook_(Blue_Moon_Inn)?oldid=27997081
class CookBlueMoonInn : Script {
    init {
        npcOperate("Talk-to", "cook_blue_moon_inn") {
            npc<Angry>("What do you want? I'm busy!")
            choice {
                option<Quiz>("Can you sell me any food?") {
                    npc<Neutral>("I suppose I could sell you some cabbage, if you're willing to pay for it. Cabbage is good for you.")
                    if (!inventory.contains("coins")) {
                        player<Sad>("Oh, I haven't got any money.")
                        npc<Angry>("Why are you asking me to sell you food if you haven't got any money? Go away!")
                        return@option
                    }
                    if (inventory.isFull()) {
                        player<Sad>("Oh, I haven't got enough space to carry it.")
                        npc<Angry>("Why are you asking me to sell you food if you can't carry it? Go away!")
                        return@option
                    }
                    choice {
                        option<Happy>("Alright, I'll buy a cabbage.") {
                            inventory.transaction {
                                remove("coins")
                                add("cabbage")
                            }
                            when (inventory.transaction.error) {
                                TransactionError.None -> npc<Neutral>("It's a deal. Now, make sure you eat it all up. Cabbage is good for you.")
                                else -> return@option
                            }
                        }
                        option<Sad>("No thanks, I don't like cabbage.") {
                            npc<Angry>("Bah! People these days only appreciate junk food.")
                        }
                    }
                }
                option<Quiz>("Can you give me any free food?") {
                    npc<Angry>("Can you give me any free money?")
                    player<Confused>("Why should I give you free money?")
                    npc<Angry>("Why should I give you free food?")
                    player<Bored>("Oh, forget it.")
                }
                option<Quiz>("I don't want anything from this horrible kitchen.") {
                    npc<Angry>("How dare you? I put a lot of effort into cleaning this kitchen. My daily sweat and elbow-grease keep this kitchen clean!")
                    player<Shock>("Ewww!")
                    npc<Angry>("Oh, just leave me alone.")
                }
            }
        }
    }
}
