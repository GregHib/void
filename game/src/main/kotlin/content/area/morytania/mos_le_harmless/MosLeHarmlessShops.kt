package content.area.morytania.mos_le_harmless

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.Script

class MosLeHarmlessShops : Script {

    init {
        npcApproach("Trade", "mike,charley,joe,smith") { (target) ->
            approachRange(2)
            target.face(this)
            val def = target.def(this)
            openShop(def["shop"])
        }
    }
}
