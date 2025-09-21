package content.area.morytania.mos_le_harmless

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.event.Script

@Script
class MosLeHarmlessShops {
    init {
        npcApproach("Trade", "mike", "charley", "joe", "smith") {
            approachRange(2)
            target.face(player)
            player.openShop(def["shop"])
        }
    }
}
