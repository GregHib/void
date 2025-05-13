package content.area.kandarin.catherby

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*

// TODO: add Merlin's Crystal quest as that how you got black candle from the shop

npcOperate("Talk-to", "candle_maker") {
    npc<Talk>("Hi! Would you be interested in some of my fine candles?")

    choice {
        option("Yes please.") {
            player<Talk>("Yes please.")
            player.openShop("candle_shop")
        }

        option("No thank you.") {
            player<Talk>("No thank you.")
            // Ends dialogue naturally
        }
    }
}

npcOperate("Trade", "candle_maker") {
    player.openShop("candle_shop")
}
