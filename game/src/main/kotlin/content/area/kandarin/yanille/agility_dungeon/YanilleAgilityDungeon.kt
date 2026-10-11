package content.area.kandarin.yanille.agility_dungeon

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory

class YanilleAgilityDungeon : Script {
    init {
        objectOperate("Open", "door_70_closed") { (target) ->
            if (tile.y < target.tile.y) {
                message("The door is locked.")
                sound("locked")
                return@objectOperate
            }
            enterDoor(target)
        }

        objectOperate("Pick-lock", "door_70_closed") { (target) ->
            if (tile.y < target.tile.y) {
                if (!has(Skill.Thieving, 82, message = true)) {
                    return@objectOperate
                }
                if (!inventory.contains("lockpick")) {
                    message("You need a lockpick for this lock.")
                    return@objectOperate
                }
                message("You attempt to pick the lock.", ChatType.Filter)
                sound("locked")
                delay(1)
                // TODO: Establish the original failure rate, as with Ardougne's locked doors.
                message("You manage to pick the lock.", ChatType.Filter)
                exp(Skill.Thieving, 50.0)
            }
            enterDoor(target)
        }
    }
}
