package content.area.tirannwn.isafdar

import content.entity.combat.hit.damage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class IsafdarTraps : Script {

    init {
        objectOperate("Step-over", "tripwire") { (target) ->
            val northSouth = target.rotation == 0 || target.rotation == 2
            crossTrap(target, northSouth, "tripwire_step_over", "You successfully step over the tripwire.", "You snag the trip wire as you step over it.")
        }

        objectOperate("Pass", "isafdar_sticks_trap") { (target) ->
            val northSouth = target.rotation == 1 || target.rotation == 3
            crossTrap(target, northSouth, "sticks_trap_pass", "You manage to skillfully pass the trap.", "You set off the trap as you pass.")
        }
    }

    suspend fun Player.crossTrap(target: GameObject, northSouth: Boolean, anim: String, success: String, failure: String) {
        // Rotation determines the trap's axis; cross to the far side relative to the player
        val direction: Direction
        val dest = if (northSouth) {
            direction = if (tile.y > target.tile.y) Direction.SOUTH else Direction.NORTH
            Tile(target.tile.x, if (tile.y > target.tile.y) target.tile.y - 1 else target.tile.y + 2, tile.level)
        } else {
            direction = if (tile.x > target.tile.x) Direction.WEST else Direction.EAST
            Tile(if (tile.x > target.tile.x) target.tile.x - 1 else target.tile.x + 2, target.tile.y, tile.level)
        }
        face(direction)
        delay()
        anim(anim)
        exactMoveDelay(dest, delay = 60, direction = direction)
        if (Level.success(levels.get(Skill.Agility), 128..250)) { // TODO unknown rate
            message(success, ChatType.Filter)
        } else {
            message(failure)
            damage(random.nextInt(20, 41)) // TODO unknown damage
        }
    }
}
