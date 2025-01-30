package content.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.type.Tile

data class ObjectTeleport(
    override val character: Player,
    val id: String,
    val tile: Tile,
    val obj: ObjectDefinition,
    val option: String
) : CancellableEvent(), Context<Player> {
    var delay: Int? = null
    var land: Boolean = false

    override val size = 5

    override val notification: Boolean = true

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_obj_teleport_${if (land) "land" else "takeoff"}"
        1 -> dispatcher.identifier
        2 -> id
        3 -> obj.stringId
        4 -> option
        else -> null
    }
}

fun objTeleportTakeOff(option: String = "*", vararg ids: String = arrayOf("*"), block: suspend ObjectTeleport.() -> Unit) {
    val handler: suspend ObjectTeleport.(Player) -> Unit = {
        block.invoke(this)
    }
    for (id in ids) {
        Events.handle("player_obj_teleport_takeoff", "player", "*", id, option, handler = handler)
    }
}

fun objTeleportLand(option: String = "*", vararg ids: String = arrayOf("*"), block: suspend ObjectTeleport.() -> Unit) {
    val handler: suspend ObjectTeleport.(Player) -> Unit = {
        block.invoke(this)
    }
    for (id in ids) {
        Events.handle("player_obj_teleport_land", "player", "*", id, option, handler = handler)
    }
}

fun objTeleport(option: String = "*", obj: String = "*", id: String = "*", land: Boolean = true, handler: suspend ObjectTeleport.() -> Unit) {
    Events.handle<Player, ObjectTeleport>("player_obj_teleport_${if (land) "land" else "takeoff"}", "player", id, obj, option) {
        handler.invoke(this)
    }
}