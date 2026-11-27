package content.minigame.castle_wars

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class CastleWarsFlag : Script {
    init {
        objectOperate("Capture", "castle_wars_saradomin_banner_stand") {
            if (equipped(EquipSlot.Cape).id == "saradomin_team_cape") {
                message("Saradomin wants the other team's standard!")
                return@objectOperate
            }
            if (inventory.isFull()) {
                message("You don't have enough free inventory space to do that.")
                return@objectOperate
            }


            // TODO unequip shield and weapon and if when failed
            inventoryFull()
            message("Their standard has already been taken!")
        }

        objTeleportTakeOff("Climb-down", "castle_wars_zamorak_spawn_trapdoor") { _, _ ->
            if (equipped(EquipSlot.Weapon).id == "saradomin_flag") {
                message("You can't take a flag into the starting room.")
                Teleport.CANCEL
            } else {
                Teleport.CONTINUE
            }
        }

        objTeleportTakeOff("Climb-down", "castle_wars_saradomin_spawn_trapdoor") { _, _ ->
            if (equipped(EquipSlot.Weapon).id == "zamorak_flag") {
                message("You can't take a flag into the starting room.")
                Teleport.CANCEL
            } else {
                Teleport.CONTINUE
            }
        }
    }
}