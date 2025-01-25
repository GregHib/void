package content.area.kharidian_desert.al_kharid

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.npc.shop.openShop

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
