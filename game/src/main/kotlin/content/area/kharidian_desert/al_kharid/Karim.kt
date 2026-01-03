package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Karim : Script {

    init {
        npcOperate("Talk-to", "karim") {
            npc<Quiz>("Would you like to buy a nice kebab? Only one gold.")
            choice {
                option<Confused>("I think I'll give it a miss.")
                option<Idle>("Yes please.") {
                    if (inventory.remove("coins", 1)) {
                        inventory.add("kebab")
                        message("You buy a kebab.")
                    } else {
                        player<Disheartened>("Oops, I forgot to bring any money with me.")
                        npc<Idle>("Come back when you have some.")
                    }
                }
            }
        }
    }
}
