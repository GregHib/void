package world.gregs.voidps.engine.suspend

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.remaining
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

/**
 * Veto when dialogue tries to override non-dialogue suspension.
 */
fun suspendDelegate(): ReadWriteProperty<Any?, Suspension?> = Delegates.vetoable(null) { _, old, value ->
    value?.dialogue != true || old?.dialogue != false
}

fun Character.resumeSuspension(): Boolean {
    val suspend = suspension ?: return false
    if (suspend.ready()) {
        suspension = null
        suspend.resume()
    }
    return true
}

suspend fun PlayerContext.stop() {
    suspendCoroutine<Unit> {
        player.suspension = null
    }
}

suspend fun CharacterContext.pause(ticks: Int = 1) {
    TickSuspension(ticks)
}

context(CharacterContext) suspend fun Player.awaitDialogues(): Boolean {
    PredicateSuspension { dialogue == null }
    return true
}

context(CharacterContext) suspend fun Player.awaitInterfaces(): Boolean {
    PredicateSuspension { menu == null }
    return true
}

suspend fun PlayerContext.pauseForever() {
    InfiniteSuspension()
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