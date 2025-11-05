package content.area.kandarin.ardougne

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject

class ArdougneDoors : Script {
    init {
        objectOperate("Open", "ardougne_locked_door_closed,ardougne_house_locked_door_closed,ardougne_castle_locked_door_closed") { (target) ->
            if (tile.x <= target.tile.x) {
                message("The door is locked.")
                sound("locked")
                return@objectOperate
            }
            message("You go through the door.", ChatType.Filter)
            enterDoor(target)
        }

        objectOperate("Pick-lock", "ardougne_locked_door_closed,ardougne_house_locked_door_closed,ardougne_castle_locked_door_closed") { (target) ->
            if (tile.x > target.tile.x) {
                message("The door is already unlocked.")
                message("You go through the door.", ChatType.Filter)
            } else if (failed(this, target)) {
                return@objectOperate
            }
            enterDoor(target)
        }

        objectOperate("Open", "chaos_druid_tower_locked_door_closed") { (target) ->
            if (tile.x >= target.tile.x) {
                message("The door is locked.")
                sound("locked")
                return@objectOperate
            }
            message("You go through the door.", ChatType.Filter)
            enterDoor(target)
        }

        objectOperate("Pick-lock", "chaos_druid_tower_locked_door_closed") { (target) ->
            if (tile.x < target.tile.x) {
                message("The door is already unlocked.")
                message("You go through the door.", ChatType.Filter)
            } else if (failed(this, target)) {
                return@objectOperate
            }
            enterDoor(target)
        }
    }

    private fun failed(player: Player, target: GameObject): Boolean {
        val level: Int = target.def.getOrNull("level") ?: return true
        if (!player.has(Skill.Thieving, level)) {
            return true
        }
        val chance = 255..255 // TODO
        player.message("You attempt to pick the lock.", ChatType.Filter)
        if (Level.success(player.levels.get(Skill.Thieving), chance)) {
            player.message("You manage to pick the lock.", ChatType.Filter)
            val exp: Double = target.def.getOrNull("exp") ?: return false
            player.exp(Skill.Thieving, exp)
        } else {
            player.message("You fail to pick the lock.")
            return true
        }
        return false
    }
}
