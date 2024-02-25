package world.gregs.voidps.world.activity.dnd.shootingstar

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject

class StarDustHandler {
    companion object {
        var handleMinedStarDust: ((GameObject) -> Unit)? = null

        var collectedDustHandler: (() -> Unit)? = null

        var isEarlyBird: ((Player) -> Boolean)? = null
        fun invokeIsEarlyBird(player: Player): Boolean? {
            return isEarlyBird?.invoke(player)
        }

        fun invokeCollectedStarDust() {
            collectedDustHandler?.invoke()
        }

        fun invokeHandleMinedStarDust(gameObject: GameObject) {
            handleMinedStarDust?.invoke(gameObject)
        }
    }
}