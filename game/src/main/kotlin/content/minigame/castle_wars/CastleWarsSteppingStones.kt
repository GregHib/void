package content.minigame.castle_wars

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject

class CastleWarsSteppingStones : Script {
    init {
        objectApproach("Jump-to", "castle_wars_steping_stone") { (target) ->
            approachRange(1)
            jump(target)
        }

        objectOperate("Jump-to", "castle_wars_steping_stone") { (target) ->
            jump(target)
        }
    }

    private suspend fun Player.jump(target: GameObject) {
        if (!tile.within(target.tile, 1)) {
            cantReach()
            return
        }
        val direction = target.tile.delta(tile).toDirection()
        anim("stepping_stone_jump", delay = 20)
        exactMoveDelay(target.tile, startDelay = 30, delay = 45, direction = direction)
        sound("jump")
    }
}