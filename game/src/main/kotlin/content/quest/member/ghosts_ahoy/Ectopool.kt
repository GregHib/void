package content.quest.member.ghosts_ahoy

import content.entity.obj.ObjectTeleports
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Direction

class Ectopool : Script {

    init {
        objTeleportTakeOff("Jump-down", "ectopool_shortcut_rail") { _, _ ->
            if (!has(Skill.Agility, 58)) {
                message("You need an agility level of at least 58 to climb down this wall.")
                return@objTeleportTakeOff Teleport.CANCEL
            }
            anim("jump_down")
            return@objTeleportTakeOff 1
        }

        objTeleportLand("Jump-down", "ectopool_shortcut_rail") { _, _ ->
            anim("jump_land")
        }

        objTeleportTakeOff("Jump-up", "ectopool_shortcut_wall") { target, option ->
            if (!has(Skill.Agility, 58)) {
                message("You need an agility level of at least 58 to climb up this wall.")
                return@objTeleportTakeOff Teleport.CANCEL
            }
            anim("jump_up")
            queue("jump_to") {
                val teleports = get<ObjectTeleports>()
                val definition = teleports.get(target.id, option).first()
                val tile = teleports.teleportTile(player, definition)
                tele(tile.addX(1))
                exactMoveDelay(tile, startDelay = 49, delay = 68, direction = Direction.WEST)
            }
            return@objTeleportTakeOff Teleport.CANCEL
        }
    }
}
