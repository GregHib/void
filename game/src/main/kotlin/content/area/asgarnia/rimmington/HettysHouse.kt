package content.area.asgarnia.rimmington

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCs

class HettysHouse : Script {
    init {
        objectOperate("Open", "trapdoor_hettys_basement_closed") {
            clearAnim()
            val hetty = NPCs.findOrNull(tile.regionLevel, "hetty_rimmington") ?: return@objectOperate
            talkWith(hetty)
            npc<Neutral>("Hey out there! I really don't think you have any business in my cellar.")
            player<Neutral>("Oh, sorry.")
        }

        objectOperate("Close", "trapdoor_hettys_basement_opened") {
            set("hettys_trapdoor_opened", false)
        }
    }
}
