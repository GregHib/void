package world.gregs.voidps.world.map.al_kharid

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

npcOperate("Talk-to", "dommik") {
    npc<Happy>("Would you like to buy some crafting equipment?")
    choice {
        option<Neutral>("No thanks; I've got all the Crafting equipment I need.") {
            npc<Happy>("Okay. Fare well on your travels.")
        }
        option<Neutral>("Let's see what you've got, then.") {
            player.openShop("dommiks_crafting_store")
        }
    }
}

npcOperate("Trade", "dommik") {
    player.openShop("dommiks_crafting_store")
}
