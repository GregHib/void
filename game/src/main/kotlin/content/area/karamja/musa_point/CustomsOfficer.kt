package content.area.karamja.musa_point

import content.entity.obj.ship.boatTravel
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Upset
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.Dialogue
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

@Script
class CustomsOfficer : Api {
    init {
        npcOperateDialogue("Talk-to", "customs_officer_brimhaven") {
            npc<Talk>("Can I help you?")
            choice {
                option<Quiz>("Can I journey on this ship?") {
                    npc<Talk>("Hey, I know you, you work at the plantation.")
                    npc<Talk>("I don't think you'll try smuggling anything, you just need to pay a boarding charge of 30 coins.")
                    choice {
                        option("Ok.") {
                            if (!player.inventory.remove("coins", 30)) {
                                player<Upset>("Oh dear, I don't seem to have enough money.")
                                return@option
                            }
                            travel()
                        }
                        option<Talk>("Oh, I'll not bother then.")
                    }
                }
                option("Does Karamja have unusual customs then?") {
                    player<Quiz>("Does Karamja have any unusual customs then?")
                    npc<Talk>("I'm not that sort of customs officer.")
                }
            }
        }

        npcOperateDialogue("Pay-Fare", "customs_officer_brimhaven") {
            if (!player.inventory.remove("coins", 30)) {
                player.message("You do not have enough money for that.")
                return@npcOperateDialogue
            }
            travel()
        }
    }

    private suspend fun Dialogue.travel() {
        player.message("You pay 30 coins and board the ship.")
        boatTravel("karamja_to_port_sarim", 7, Tile(3032, 3217, 1))
        statement("The ship arrives at Port Sarim.")
    }
}
