package content.area.asgarnia.dwarven_mines

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class DrogoDwarf : Script {

    private val shop = "drogos_mining_emporium"

    init {
        npcOperate("Talk-to", "drogo_dwarf") {
            npc<Happy>("'Ello. Welcome to my Mining shop, friend.")
            menu()
        }
    }

    private suspend fun Player.menu() {
        choice {
            option<Neutral>("Do you want to trade?") {
                openShop(shop)
            }
            option<Happy>("Hello shorty.") {
                npc<Angry>("I may be short but at least I've got manners.")
            }
            option<Quiz>("Why don't you ever restock ores and bars?") {
                npc<Happy>("The only ores and bars I sell are those sold to me.")
            }
        }
    }
}
