package content.area.asgarnia.port_sarim

import content.entity.obj.ship.boatTravel
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Upset
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class Seaman : Script {
    init {
        npcOperate("Talk-to", "seaman_lorris*,captain_tobias*,seaman_thresnor*") {
            npc<Quiz>("Do you want to go on a trip to Karamja?")
            npc<Talk>("The trip will cost you 30 coins.")
            choice {
                option<Happy>("Yes please.") {
                    if (!inventory.remove("coins", 30)) {
                        player<Upset>("Oh dear, I don't seem to have enough money.")
                        return@option
                    }
                    travel()
                }
                option<Talk>("No, thank you.")
            }
        }

        npcOperate("Pay-fare", "seaman_lorris*,captain_tobias*,seaman_thresnor*") {
            if (!inventory.remove("coins", 30)) {
                message("You do not have enough money for that.")
                return@npcOperate
            }
            travel()
        }
    }

    private suspend fun Player.travel() {
        message("You pay 30 coins and board the ship.")
        boatTravel("port_sarim_to_karamja", 7, Tile(2956, 3143, 1))
        statement("The ship arrives at Karamja.")
    }
}
