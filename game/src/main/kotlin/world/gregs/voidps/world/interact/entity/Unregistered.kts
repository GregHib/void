import kotlinx.coroutines.Job
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.event.on


on<Entity, Unregistered>({ it.contains("delays") }) { character ->
    val delays: Set<Job> = character.remove("delays") ?: return@on
    for (delay in delays) {
        delay.cancel()
    }
}