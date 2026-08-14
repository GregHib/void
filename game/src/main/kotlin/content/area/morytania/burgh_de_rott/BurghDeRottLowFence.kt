package content.area.morytania.burgh_de_rott

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.obstacle
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Direction

class BurghDeRottLowFence : Script {

    init {
        objectOperate("Jump-over", "burgh_de_rott_low_fence") { (target) ->
            if (!has(Skill.Agility, 25)) {
                obstacle(25)
                return@objectOperate
            }
            jump(target)
        }
    }

    private suspend fun Player.jump(target: GameObject) {
        val direction = if (tile.x < target.tile.x) Direction.EAST else Direction.WEST
        val landing = if (direction == Direction.EAST) target.tile else target.tile.add(Direction.WEST)
        // Take a run up so the jump covers two tiles, the animation is too long for a single step
        walkOverDelay(landing.minus(direction).minus(direction))
        face(direction)
        delay()
        anim("low_fence_jump")
        exactMoveDelay(landing, 60, direction = direction)
    }
}
