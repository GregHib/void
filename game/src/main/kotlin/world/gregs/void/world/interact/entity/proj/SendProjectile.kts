import world.gregs.void.engine.action.Scheduler
import world.gregs.void.engine.action.delay
import world.gregs.void.engine.entity.Registered
import world.gregs.void.engine.entity.Unregistered
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.item.offset
import world.gregs.void.engine.entity.proj.Projectile
import world.gregs.void.engine.entity.proj.Projectiles
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.event.then
import world.gregs.void.engine.map.chunk.ChunkBatcher
import world.gregs.void.network.codec.game.encode.ProjectileAddEncoder
import world.gregs.void.utility.inject
import world.gregs.void.world.interact.entity.proj.ShootProjectile

val projectiles: Projectiles by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()
val addEncoder: ProjectileAddEncoder by inject()

ShootProjectile then {
    var index = if (target != null) target.index + 1 else 0
    if (target is Player) {
        index = -index
    }
    val projectile = Projectile(id, tile, direction, index, delay, flightTime, startHeight, endHeight, curve, offset)
    projectiles.add(projectile)
    batcher.update(tile.chunk, projectile.toMessage())
    decay(projectile)
    bus.emit(Registered(projectile))
}

/**
 * Reduces timers to keep approx in sync for players starting to view mid-way through
 */
fun decay(projectile: Projectile) {
    projectile.job = scheduler.add {
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
        bus.emit(Unregistered(projectile))
    }
}

fun Projectile.toMessage(): (Player) -> Unit = { player ->
    addEncoder.encode(
        player,
        tile.offset(3),
        id,
        direction.x,
        direction.y,
        index,
        startHeight,
        endHeight,
        delay,
        delay + flightTime,
        curve,
        offset
    )
}

batcher.addInitial { player, chunk, messages ->
    projectiles[chunk].forEach {
        if (it.visible(player)) {
            messages += it.toMessage()
        }
    }
}