import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.Unregistered
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.item.offset
import rs.dusk.engine.entity.proj.Projectile
import rs.dusk.engine.entity.proj.Projectiles
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.map.chunk.ChunkBatcher
import rs.dusk.network.rs.codec.game.encode.message.ProjectileAddMessage
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.proj.ShootProjectile

val projectiles: Projectiles by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()

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

fun Projectile.toMessage() = ProjectileAddMessage(
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

batcher.addInitial { player, chunk, messages ->
    projectiles[chunk].forEach {
        if (it.visible(player)) {
            messages += it.toMessage()
        }
    }
}