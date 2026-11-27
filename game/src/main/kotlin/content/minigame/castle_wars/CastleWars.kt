package content.minigame.castle_wars

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class CastleWars : Script {
    init {
        // TODO if leave and rejoin flag count is reset.
        // TODO use bandages on friendly
//        message("You don't want to be healing your enemies!")
        // 30 minute rotation, 5 in lobby, 25 in game
        objectOperate("Enter", "saradomin_portal_castle_wars") {
            // https://youtu.be/fMI0QS8HX24?t=748
            message("The Saradomin team is powerful enough already! Guthix demands balance - join the Zamorak team instead!")
        }

        objectOperate("Enter", "guthix_portal_castle_wars") {
        }


        objectOperate("Enter", "zamorak_portal_castle_wars") {
            message("The Zamorak team is powerful enough already! Guthix demands balance - join the Saradomin team instead!")
            statement("I pity your faith in my brothers. I shall bless you with some time in the most holy of forms; maybe its wisdom will rub off on you and you'll see the errors of your ways.")
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
                "zamorak_team_cape" -> {
                    equipment.remove(EquipSlot.Weapon.index, "saradomin_flag")
                    close("castle_wars_status_overlay_zamorak")
                }
                "saradomin_team_cape" -> {
                    equipment.remove(EquipSlot.Weapon.index, "zamorak_flag")
                    close("castle_wars_status_overlay_saradomin")
                }
                else -> return@exited
            }
            for (item in items) {
                inventory.removeToLimit(item, 28)
            }
        }


        // https://youtu.be/c6Wn-zKCEEI?t=214
        // message("Your familiar has been killed.")
        /*
            You've been awarded 4 tickets for your contributions.
            You now have 7 tickets.
            <red>You've won 2 games of Castle Wars!
         */
    }

    companion object {
        private val items = listOf(
            "rock_castle_wars",
            "explosive_potion",
            "climbing_rope",
            "bandages_castle_wars",
            "toolkit_castle_wars",
            "barricade_castle_wars",
            "flare",
            "bucket",
            "tinderbox",
            "bronze_pickaxe",
        )
    }
}