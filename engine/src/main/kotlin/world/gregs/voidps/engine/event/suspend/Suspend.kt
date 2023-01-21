package world.gregs.voidps.engine.event.suspend

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.remaining
import kotlin.coroutines.suspendCoroutine

suspend fun PlayerContext.stop() {
    suspendCoroutine<Unit> {
        player.queue.suspend = null
    }
}

suspend fun PlayerContext.pause(ticks: Int = 1) {
    if (ticks <= 0) {
        return
    }
    suspendCancellableCoroutine {
        player.queue.suspend = TickSuspension(ticks, it)
    }
}

context(PlayerContext) suspend fun Player.awaitDialogues(): Boolean {
    PredicateSuspension { dialogue == null }
    return true
}

context(PlayerContext) suspend fun Player.awaitInterfaces(): Boolean {
    PredicateSuspension { menu == null }
    return true
}

suspend fun PlayerContext.delayForever() {
    suspendCancellableCoroutine<Unit> {
        player.queue.suspend = InfiniteSuspension
    }
}

suspend fun PlayerContext.arriveDelay() {
    val delay = player.remaining("last_movement").toInt()
    if (delay == -1) {
        return
    }
    pause(delay)
}

context(PlayerContext) fun Player.approachRange(range: Int?): Unit? {
    val interact = mode as? Interact ?: return Unit
    if (interact.approachRange != range) {
        interact.approachRange = range
        return null
    }
    return Unit
}

private val logger = InlineLogger()

context(PlayerContext) suspend fun Player.playAnimation(id: String, override: Boolean = false) {
    val ticks = setAnimation(id, override = override)
    if (ticks == -1) {
        logger.warn { "No animation delay $id" }
    } else {
        pause(ticks)
    }
}