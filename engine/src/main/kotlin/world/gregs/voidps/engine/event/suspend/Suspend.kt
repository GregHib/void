package world.gregs.voidps.engine.event.suspend

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.remaining
import world.gregs.voidps.engine.event.SuspendableEvent
import kotlin.coroutines.suspendCoroutine

suspend fun SuspendableEvent.stop() {
    suspendCoroutine<Unit> {
        suspend = null
    }
}

suspend fun SuspendableEvent.delay(ticks: Int = 1) {
    suspendCancellableCoroutine {
        suspend = TickSuspension(ticks, it)
    }
}

context(SuspendableEvent) suspend fun Player.arriveDelay() {
    val delay = remaining("last_movement").toInt()
    if (delay == -1) {
        return
    }
    delay(delay)
}

context(SuspendableEvent) fun Player.approachRange(range: Int?): Unit? {
    val interact = mode as? Interact ?: return Unit
    if (interact.approachRange != range) {
        interact.approachRange = range
        return null
    }
    return Unit
}