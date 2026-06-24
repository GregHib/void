package content.area.karamja.musa_point

import content.entity.obj.ship.boatTravel
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class CustomsOfficer : Script {

    init {
        npcOperate("Talk-to", "customs_officer_brimhaven") { (officer) ->
            npc<Neutral>("Can I help you?")
            choice {
                option<Quiz>("Can I journey on this ship?") {
                    npc<Neutral>("Hey, I know you, you work at the plantation.")
                    npc<Neutral>("I don't think you'll try smuggling anything, you just need to pay a boarding charge of 30 coins.")
                    choice {
                        option("Ok.") {
                            if (!inventory.remove("coins", 30)) {
                                player<Sad>("Oh dear, I don't seem to have enough money.")
                                return@option
                            }
                            travel(officer)
                        }
                        option<Neutral>("Oh, I'll not bother then.")
                    }
                }
                option("Does Karamja have unusual customs then?") {
                    player<Quiz>("Does Karamja have any unusual customs then?")
                    npc<Neutral>("I'm not that sort of customs officer.")
                }
            }
        }

        npcOperate("Pay-Fare", "customs_officer_brimhaven") { (officer) ->
            if (!inventory.remove("coins", 30)) {
                message("You do not have enough money for that.")
                return@npcOperate
            }
            travel(officer)
        }
    }

    private suspend fun Player.travel(officer: NPC) {
        message("You pay 30 coins and board the ship.")
        if (officer.tile in Areas["brimhaven"]) {
            boatTravel("brimhaven_to_ardougne", 7, Tile(2683, 3268, 1))
            statement("The ship arrives at Ardougne.")
        } else {
            boatTravel("karamja_to_port_sarim", 7, Tile(3032, 3217, 1))
            statement("The ship arrives at Port Sarim.")
        }
    }
}
