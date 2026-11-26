package content.minigame.castle_wars

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject

class CastleWarsSteppingStones : Script {
    init {
        objectApproach("Jump-to", "castle_wars_steping_stone") { (target) ->
            // TODO don't allow jumping multiple ahead
            approachRange(1)
            jump(target)
        }

        objectOperate("Jump-to", "castle_wars_steping_stone") { (target) ->
            jump(target)
        }
    }

    private suspend fun Player.jump(target: GameObject) {
        // TODO proper movement
        val direction = target.tile.delta(tile).toDirection()
        anim("stepping_stone_step", delay = 20)
        exactMoveDelay(target.tile, startDelay = 48, delay = 60, direction = direction)
        sound("jump", delay = 30)
        delay()
    }
}