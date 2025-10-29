package content.area.morytania.mos_le_harmless

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.mode.interact.approachRange
import world.gregs.voidps.engine.event.Script

@Script
class MosLeHarmlessShops : Api {
    init {
        npcApproach("Trade", "mike,charley,joe,smith") { player, target ->
            player.approachRange(2)
            target.face(player)
            player.openShop(target.def["shop"])
        }
    }
}
