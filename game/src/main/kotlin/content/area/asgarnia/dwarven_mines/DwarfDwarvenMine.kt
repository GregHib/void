package content.area.asgarnia.dwarven_mines

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class DwarfDwarvenMine : Script {

    private val shop = "dwarven_shopping_store"

    init {
        npcOperate("Talk-to", "dwarf_dwarven_mine") {
            npc<Quiz>("Can I help you at all?")
            choice {
                option("Yes please, what are you selling?") {
                    openShop(shop)
                }
                option<Idle>("No thanks.")
            }
        }
    }
}
