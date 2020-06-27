import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.entity.obj.ObjectOption
import rs.dusk.utility.inject
import rs.dusk.world.entity.obj.ClearObject
import rs.dusk.world.entity.obj.ReplaceObject

val loader: FileLoader by inject()
val bus: EventBus by inject()

val doorCloseDelay = 500

val doors: Map<Int, Int> = loader.load<Map<String, Int>>("./cache/data/doors.yml")!!.mapKeys { it.key.toInt() }

fun Location.isDoor() = def.name.startsWith("Door")

ObjectOption where { location.isDoor() && option == "Open" } then {
    val replacement = doors[location.id]
    if(replacement == null) {
        bus.emit(ClearObject(location))
        return@then
    }
    rotate(replacement, 1)
}

ObjectOption where { location.isDoor() && option == "Close" } then {
    // TODO limit door closing in quick-succession to 5 times before the door is stuck for that player
    val replacement = doors[location.id]
    if(replacement == null) {
        bus.emit(ClearObject(location))
        return@then
    }
    rotate(replacement, -1)
}

fun ObjectOption.rotate(replacement: Int, clockwise: Int) {
    val orientation = Direction.cardinal[location.rotation - clockwise and 0x3]
    val tile = location.tile.add(orientation.delta)
    val rotation = (location.rotation + clockwise) and 0x3
    bus.emit(ReplaceObject(location, replacement, tile, location.type, rotation, doorCloseDelay))
}