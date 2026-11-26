package content.minigame.castle_wars

import content.entity.obj.door.closeDoor
import content.entity.obj.door.openDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class CastleWarsDoors : Script {
    init {
        objectOperate("Open", "castle_wars_zamorak_main_door_*_closed") { (target) ->
            if (equipped(EquipSlot.Cape).id == "saradomin_team_cape") {
                // https://youtu.be/yWkyD0WOJK8?t=346
                message("It's shut. You'll have to break it down.")
                return@objectOperate
            }
            openDoor(target)
        }

        objectOperate("Open", "castle_wars_saradomin_main_door_*_closed") { (target) ->
            if (equipped(EquipSlot.Cape).id == "zamorak_team_cape") {
                // https://youtu.be/yWkyD0WOJK8?t=346
                message("It's shut. You'll have to break it down.")
                return@objectOperate
            }
            openDoor(target)
        }

        objectOperate("Attack", "castle_wars_zamorak_main_door_*_closed") { (target) ->
            if (equipped(EquipSlot.Cape).id == "saradomin_team_cape") {
                // TODO anim
            } else {
                message("You don't want to attack your own teams door.")// TODO proper message
            }
        }

        objectOperate("Attack", "castle_wars_saradomin_main_door_*_closed") { (target) ->
            if (equipped(EquipSlot.Cape).id == "zamorak_team_cape") {
                // TODO anim
            } else {
                message("You don't want to attack your own teams door.")// TODO proper message
            }
        }

        objectOperate("Unlock", "castle_wars_zamorak_side_door_closed") { (target) ->
            if (equipped(EquipSlot.Cape).id == "zamorak_team_cape") {
                openDoor(target)
            } else {
                // TODO
            }
        }

        objectOperate("Unlock", "castle_wars_saradomin_side_door_closed") { (target) ->
            if (equipped(EquipSlot.Cape).id == "saradomin_team_cape") {
                openDoor(target)
            } else {
                // TODO
            }
        }

        objectOperate("Lock", "castle_wars_zamorak_side_door_opened") { (target) ->
            closeDoor(target)
        }

        objectOperate("Lock", "castle_wars_saradomin_side_door_opened") { (target) ->
            closeDoor(target)
        }
    }
}