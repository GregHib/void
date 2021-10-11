import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.proj.Projectile
import world.gregs.voidps.engine.entity.proj.Projectiles
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.network.encode.addProjectile
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile

val projectiles: Projectiles by inject()
val scheduler: Scheduler by inject()
val store: EventHandlerStore by inject()
val batches: ChunkBatches by inject()

on<World, ShootProjectile> {
    var index = if (target != null) target.index + 1 else 0
    if (target is Player) {
        index = -index
    }
    val projectile = Projectile(
        id = id,
        tile = tile,
        direction = direction,
        index = index,
        delay = delay,
        flightTime = flightTime,
        startHeight = startHeight,
        endHeight = endHeight,
        curve = curve,
        offset = offset
    )
    store.populate(projectile)
    projectiles.add(projectile)
    batches.update(tile.chunk, addProjectile(projectile))
    decay(projectile)
    projectile.events.emit(Registered)
}

/**
 * Reduces timers to keep approx in sync for players starting to view mid-way through
 */
fun decay(projectile: Projectile) {
    projectile.job = scheduler.launch {
        repeat(projectile.delay / 30) {
            delay(1)
            projectile.delay -= 30
        }
        projectile.delay = 0
        repeat(projectile.flightTime / 30) {
            delay(1)
            projectile.flightTime -= 30
        }
        projectile.flightTime = 0
        projectiles.remove(projectile)
        projectile.events.emit(Unregistered)
    }
}