package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

fun Character.resumeSuspension(): Boolean {
    val suspend = suspension ?: return false
    if (suspend is Suspension.Delay && suspend.ready()) {
        suspension = null
        suspend.resume()
    }
    if (suspend is Suspension.Custom && suspend.ready()) {
        suspension = null
        suspend.resume()
    }
    return true
}

suspend fun Player.awaitDialogues(): Boolean {
    suspendCancellableCoroutine {
        suspension = Suspension.Custom(it) { dialogue == null }
    }
    suspension = null
    return true
}

suspend fun Player.pauseButton() {
    suspendCancellableCoroutine {
        suspension = Suspension.Continue(it)
    }
    suspension = null
}

suspend fun Player.pauseString(): String {
    val string = suspendCancellableCoroutine {
        suspension = Suspension.StringEntry(it)
    }
    suspension = null
    return string
}

suspend fun Player.pauseInt(): Int {
    val int = suspendCancellableCoroutine {
        suspension = Suspension.IntEntry(it)
    }
    suspension = null
    return int
}
