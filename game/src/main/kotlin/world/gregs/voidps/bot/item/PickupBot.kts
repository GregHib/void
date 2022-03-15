import kotlinx.coroutines.CancellableContinuation
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.event.on
import kotlin.coroutines.resume

on<Unregistered>({ it.contains("bot_jobs") }) { floorItem: FloorItem ->
    val jobs: Set<CancellableContinuation<Unit>> = floorItem.remove("bot_jobs") ?: return@on
    for (job in jobs) {
        job.resume(Unit)
    }
}