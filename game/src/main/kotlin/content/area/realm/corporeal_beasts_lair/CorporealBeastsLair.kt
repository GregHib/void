package content.area.realm.corporeal_beasts_lair

import content.entity.player.dialogue.type.warning
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Players

class CorporealBeastsLair : Script {
    init {
        objectOperate("Go-through", "corporeal_beast_lair_passage") { (target) ->
            if (tile.x <= target.tile.x) {
                tele(target.tile.add(3, 2))
            } else {
                tele(target.tile.add(-1, 2))
            }
        }

        objectOperate("Go-through", "corporeal_beast_lair_door") { (target) ->
            if (tile.x <= target.tile.x) {
                if (warning("corporeal_beast")) {
                    tele(target.tile.add(3, 2))
                }
            } else {
                tele(target.tile.add(-1, 2))
            }
        }

        objectOperate("Peek-in", "corporeal_beast_lair_door") { (target) ->
            // TODO proper message
            val lair = Areas["corporeal_beasts_lair"]
            if (Players.count { it.tile in lair } > 0) {
                message("The lair is occupied with foes trying to defeat the beast.")
            } else {
                message("The lair is empty.")
            }
        }
    }
}
