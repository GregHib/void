package content.entity.obj

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class TrapDoors : Script {

    companion object {
        private val openHandlers = linkedMapOf<String, suspend Player.(GameObject) -> Boolean>()
        private val closeHandlers = linkedMapOf<String, suspend Player.(GameObject) -> Boolean>()

        fun registerOpenHandler(key: String, handler: suspend Player.(GameObject) -> Boolean) {
            openHandlers[key] = handler
        }

        fun registerCloseHandler(key: String, handler: suspend Player.(GameObject) -> Boolean) {
            closeHandlers[key] = handler
        }

        private suspend fun Player.handleOpen(target: GameObject): Boolean {
            for (handler in openHandlers.values) {
                if (handler(target)) {
                    return true
                }
            }
            return false
        }

        private suspend fun Player.handleClose(target: GameObject): Boolean {
            for (handler in closeHandlers.values) {
                if (handler(target)) {
                    return true
                }
            }
            return false
        }
    }

    init {
        objectOperate("Open", "trapdoor_*_closed") { (target) ->
            if (handleOpen(target)) {
                return@objectOperate
            }
            anim("open_chest")
            if (target.def.transforms != null) {
                return@objectOperate
            }
            target.replace(target.id.replace("_closed", "_opened"), ticks = TimeUnit.MINUTES.toTicks(3))
        }

        objectOperate("Close", "trapdoor_*_opened") { (target) ->
            if (handleClose(target)) {
                return@objectOperate
            }
            anim("close_chest")
            if (target.def.transforms != null) {
                return@objectOperate
            }
            target.replace(target.id.replace("_opened", "_closed"), ticks = TimeUnit.MINUTES.toTicks(3))
        }
    }
}
