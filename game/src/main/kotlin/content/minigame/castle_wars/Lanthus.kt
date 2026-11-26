package content.minigame.castle_wars

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open

class Lanthus : Script {
    init {
        npcOperate("Trade-with", "lanthus") {
            open("castle_wars_shop")
        }

        interfaceOpened("castle_wars_shop") {
            open("castle_wars_shopside")
        }

        interfaceClosed("castle_wars_shop") {
            close("castle_wars_shopside")
        }
    }
}