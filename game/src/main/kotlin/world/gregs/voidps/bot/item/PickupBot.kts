import kotlinx.coroutines.CancellableContinuation
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.on
import kotlin.coroutines.resume

on<Unregistered>({ it.botJobs != null }) { floorItem: FloorItem ->
    val jobs: Set<CancellableContinuation<Unit>> = floorItem.botJobs ?: return@on
    for (job in jobs) {
        job.resume(Unit)
    }
}