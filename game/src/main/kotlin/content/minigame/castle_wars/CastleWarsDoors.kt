package content.minigame.castle_wars

import content.entity.obj.door.DoubleDoor
import content.entity.obj.door.closeDoor
import content.entity.obj.door.openDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

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
            if (equipped(EquipSlot.Cape).id != "saradomin_team_cape") {
                message("You don't want to damage your own doors.")
                return@objectOperate
            }
            attack(target)
        }

        objectOperate("Attack", "castle_wars_saradomin_main_door_*_closed") { (target) ->
            if (equipped(EquipSlot.Cape).id != "zamorak_team_cape") {
                message("You don't want to damage your own doors.")
                return@objectOperate
            }
            attack(target)
        }

        objectOperate("Unlock", "castle_wars_zamorak_side_door_closed") { (target) ->
            if (equipped(EquipSlot.Cape).id == "zamorak_team_cape") {
                openDoor(target)
            } else {
                // TODO chances
                message("You manage to pick the lock!")
                openDoor(target)
            }
        }

        objectOperate("Unlock", "castle_wars_saradomin_side_door_closed") { (target) ->
            if (equipped(EquipSlot.Cape).id == "saradomin_team_cape") {
                openDoor(target)
            } else {
                // TODO chances
                message("You manage to pick the lock!")
                openDoor(target)
            }
        }

        objectOperate("Lock", "castle_wars_zamorak_side_door_opened") { (target) ->
            closeDoor(target)
        }

        objectOperate("Lock", "castle_wars_saradomin_side_door_opened") { (target) ->
            closeDoor(target)
        }

        objectOperate("Repair", "castle_wars_saradomin_broken_door_*") { (target) ->
            if (!inventory.contains("toolkit_castle_wars")) {
                message("You need a toolkit to repair the doors.")
                return@objectOperate
            }
            repair(target, "saradomin")
        }

        objectOperate("Repair", "castle_wars_zamorak_broken_door_*") { (target) ->
            if (!inventory.contains("toolkit_castle_wars")) {
                message("You need a toolkit to repair the doors.")
                return@objectOperate
            }
            repair(target, "zamorak")
        }

        itemOnObjectOperate("toolkit_castle_wars", "castle_wars_zamorak_broken_door_*") { (target) ->
            repair(target, "zamorak")
        }

        itemOnObjectOperate("toolkit_castle_wars", "castle_wars_saradomin_broken_door_*") { (target) ->
            repair(target, "saradomin")
        }
    }

    private fun Player.repair(target: GameObject, team: String) {
        if (equipped(EquipSlot.Cape).id == "${team}_team_cape") {
            // TODO anim
            val double = DoubleDoor.get(this, target, target.def, 1) ?: return
            DoubleDoor.close(this, target, target.def, double, -1) { "${it.stringId.replace("_broken", "_main")}_closed" }
        } else {
            message("You don't want to repair your opponents door.")
        }
    }

    private suspend fun Player.attack(obj: GameObject) {
        for (i in 0 until 20) {
            if (!GameObjects.contains(obj)) {
                break
            }
            anim("castle_wars_attack_door")
            if (random.nextInt(15) == 0) { // TODO proper rate
                val double = DoubleDoor.get(this, obj, obj.def, 0) ?: break
                val def = ObjectDefinitions.get(obj.id.replace("_main", "_broken").removeSuffix("_closed"))
                DoubleDoor.open(this, obj, def, double, -1, convert = {
                    it.stringId.replace("_main", "_broken").removeSuffix("_closed")
                })
                break
            }
            delay(3)
        }
    }
}