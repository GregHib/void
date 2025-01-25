package content.area.kharidian_desert.al_kharid

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

npcOperate("Talk-to", "louie_legs") {
    npc<Neutral>("Hey, wanna buy some armour?")
    choice {
        option<Neutral>("What have you got?") {
            npc<Happy>("I provide items to help you keep your legs!")
            player.openShop("louies_armoured_legs_bazaar")
        }
        option<Neutral>("No, thank you.")
    }
}
