import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.character.update.visual.player.*
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.utility.inject

val files: FileLoader by inject()

data class TeleportBuilder(
    val id: Int,
    val option: String,
    val tile: TeleTile,
    val delta: TeleTile
) {
    data class TeleTile(val x: Int, val y: Int, val plane: Int = 0)

    fun build() = Teleport(id, option, Tile(tile.x, tile.y, tile.plane), Delta(delta.x, delta.y, delta.plane))
}

@JsonDeserialize(builder = TeleportBuilder::class)
data class Teleport(val id: Int, val option: String, val tile: Tile, val delta: Delta)


fun load(array: Array<Teleport>): Map<String, Teleport> {
    val map = mutableMapOf<String, Teleport>()
    for (tele in array) {
        map["${tele.id}:${tele.tile}:${tele.option}"] = tele
    }
    return map
}

val teleports: Map<String, Teleport> = load(files.load(getProperty("stairsPath")))

ObjectOption then {
    val tele = teleports["${obj.id}:${obj.tile}:${option}"] ?: return@then
    player.action(ActionType.Climb) {
        if (obj.def.name.contains("ladder", true) || obj.def.name.contains("trapdoor", true)) {
            player.setAnimation(if (option == "Climb-down") 827 else 828)
            delay(2)
        }
        player.move(tele.delta)
    }
}

ObjectOption where { option == "Climb" } then {
}