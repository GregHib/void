package content.area.morytania.mos_le_harmless

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.npcApproach

class MosLeHarmlessShops : Script {
    init {
        npcApproach("Trade", "mike", "charley", "joe", "smith") {
            approachRange(2)
            target.face(player)
            player.openShop(def["shop"])
        }
    }
}
