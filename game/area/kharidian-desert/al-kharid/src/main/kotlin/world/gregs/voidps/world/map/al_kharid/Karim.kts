package world.gregs.voidps.world.map.al_kharid

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "karim") {
    npc<Quiz>("Would you like to buy a nice kebab? Only one gold.")
    choice {
        option<Uncertain>("I think I'll give it a miss.")
        option<Neutral>("Yes please.") {
            if (player.inventory.remove("coins", 1)) {
                player.inventory.add("kebab")
                player.message("You buy a kebab.")
            } else {
                player<Sad>("Oops, I forgot to bring any money with me.")
                npc<Neutral>("Come back when you have some.")
            }
        }
    }
}