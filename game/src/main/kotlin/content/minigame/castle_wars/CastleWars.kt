package content.minigame.castle_wars

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class CastleWars : Script {
    init {

        objectOperate("Enter", "saradomin_portal_castle_wars") {
            // https://youtu.be/fMI0QS8HX24?t=748
            message("The Saradomin team is powerful enough already! Guthix demands balance - join the Zamorak team instead!")
        }

        objectOperate("Enter", "guthix_portal_castle_wars") {
        }


        objectOperate("Enter", "zamorak_portal_castle_wars") {
            message("The Zamorak team is powerful enough already! Guthix demands balance - join the Saradomin team instead!")
        }

        objectOperate("", "") {
            // https://youtu.be/s-MRj2b37PU?t=51
            choice("There's a free space - do you want to join?") {
                option("Yes, please!") {

                }
                option("No, thanks.") {

                }
            }
        }

        entered("castle_wars") {
            when (equipped(EquipSlot.Cape).id) {
                "zamorak_team_cape" -> {
                    options.set(1, "Attack")
                    open("castle_wars_status_overlay_zamorak")
                }
                "saradomin_team_cape" -> {
                    options.set(1, "Attack")
                    open("castle_wars_status_overlay_saradomin")
                }
            }
        }

        exited("castle_wars") {
            options.remove("Attack")
            when (equipped(EquipSlot.Cape).id) {
                "zamorak_team_cape" -> close("castle_wars_status_overlay_zamorak")
                "saradomin_team_cape" -> close("castle_wars_status_overlay_saradomin")
            }
        }


        // https://youtu.be/c6Wn-zKCEEI?t=214
        // message("Your familiar has been killed.")
    }
}