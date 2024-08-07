package world.gregs.voidps.world.map.al_kharid

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

npcOperate("Talk-to", "ranael") {
    npc<Neutral>("Do you want to buy any armoured skirts? Designed especially for ladies who like to fight.")
    choice {
        option<Neutral>("Yes please.") {
            player.openShop("ranaels_super_skirt_store")
        }
        option<Neutral>("No thank you, that's not my scene.")
    }
}

npcOperate("Trade", "ranael") {
    player.openShop("ranaels_super_skirt_store")
}
