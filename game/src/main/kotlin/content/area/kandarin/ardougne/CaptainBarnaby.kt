package content.area.kandarin.ardougne

import content.entity.obj.ship.boatTravel
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
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

class CaptainBarnaby : Script {

    init {
        npcOperate("Talk-to", "captain_barnaby_2") {
            npc<Quiz>("Do you want to go on a trip to Brimhaven?")
            npc<Neutral>("The trip will cost you 30 coins.")
            choice {
                option<Happy>("Yes please.") {
                    if (!inventory.remove("coins", 30)) {
                        player<Sad>("Oh dear, I don't seem to have enough money.")
                        return@option
                    }
                    travel()
                }
                option<Neutral>("No, thank you.")
            }
        }

        npcOperate("Pay-fare", "captain_barnaby_2") {
            if (!inventory.remove("coins", 30)) {
                message("You do not have enough money for that.")
                return@npcOperate
            }
            travel()
        }

        // Boarding the docked ship doesn't set sail; the trip starts by talking to the operator.
        objTeleportLand("Cross", "gangplank_ardougne_enter") { _, _ ->
            message("You must speak to Captain Barnaby before it will set sail.")
        }
        objTeleportLand("Cross", "gangplank_brimhaven_enter") { _, _ ->
            message("You must speak to the Customs officer before it will set sail.")
        }

        objectOperate("Climb-down", "captain_barnaby_ship_ladder") { (ladder) ->
            if (ladder.tile != Tile(2682, 3267, 1)) {
                return@objectOperate
            }
            message("I don't think Captain Barnaby wants me going down there.")
        }
    }

    private suspend fun Player.travel() {
        message("You pay 30 coins and board the ship.")
        boatTravel("ardougne_to_brimhaven", 5, Tile(2775, 3234, 1))
        statement("The ship arrives at Brimhaven.")
    }
}
