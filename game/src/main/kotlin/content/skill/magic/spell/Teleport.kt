package content.skill.magic.spell

import content.entity.sound.sound
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Tile

data class Teleport(
    override val character: Player,
    val tile: Tile,
    val type: String
) : CancellableEvent(), Context<Player> {
    var land: Boolean = false

    override val size = 3

    override val notification: Boolean = true

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_teleport_${if (land) "land" else "takeoff"}"
        1 -> dispatcher.identifier
        2 -> type
        else -> null
    }

    companion object {
        fun teleport(player: Player, area: String, type: String) {
            teleport(player, get<AreaDefinitions>()[area].random(player)!!, type)
        }

        fun teleport(player: Player, tile: Tile, type: String) {
            if (player.queue.contains("teleport")) {
                return
            }
            player.closeInterfaces()
            player.strongQueue("teleport", onCancel = null) {
                val teleport = Teleport(player, tile, type)
                player.emit(teleport)
                if (teleport.cancelled) {
                    return@strongQueue
                }
                player.steps.clear()
                player.sound("teleport")
                player.gfx("teleport_$type")
                player.animDelay("teleport_$type")
                player.tele(tile)
                delay(1)
                player.sound("teleport_land")
                player.gfx("teleport_land_$type")
                player.animDelay("teleport_land_$type")
                if (type == "ancient" || type == "ectophial") {
                    delay(1)
                    player.clearAnim()
                }
                teleport.land = true
                player.emit(teleport)
            }
        }
    }
}

fun teleportTakeOff(vararg types: String = arrayOf("*"), block: suspend Teleport.() -> Unit) {
    val handler: suspend Teleport.(Player) -> Unit = {
        block.invoke(this)
    }
    for (type in types) {
        Events.handle("player_teleport_takeoff", "player", type, handler = handler)
    }
}

fun teleportLand(vararg types: String = arrayOf("*"), block: suspend Teleport.() -> Unit) {
    val handler: suspend Teleport.(Player) -> Unit = {
        block.invoke(this)
    }
    for (type in types) {
        Events.handle("player_teleport_land", "player", type, handler = handler)
    }
}