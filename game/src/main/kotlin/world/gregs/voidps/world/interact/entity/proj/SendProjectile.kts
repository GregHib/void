import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.proj.Projectile
import world.gregs.voidps.engine.entity.proj.Projectiles
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.chunk.ChunkUpdate
import world.gregs.voidps.network.encode.addProjectile
import world.gregs.voidps.utility.inject
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
    val update = addProjectile(projectile)
    projectile["update"] = update
    batches.update(tile.chunk, update)
    batches.addInitial(tile.chunk, update)
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
        // Could do something complex here to move the projectile if it changes chunk
        // probably not worth the effort
        repeat(projectile.flightTime / 30) {
            delay(1)
            projectile.flightTime -= 30
        }
        projectile.flightTime = 0
        projectiles.remove(projectile)
        projectile.events.emit(Unregistered)
        projectile.remove<ChunkUpdate>("update")?.let {
            batches.removeInitial(projectile.tile.chunk, it)
        }
    }
}