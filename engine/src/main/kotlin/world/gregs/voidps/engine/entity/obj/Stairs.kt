package world.gregs.voidps.engine.entity.obj

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.dsl.module
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import kotlin.coroutines.resume

val stairsModule = module {
    single(createdAtStart = true) { Stairs(getProperty("stairsPath"), get()) }
}

class Stairs(
    private val path: String,
    private val files: FileLoader
) {

    private lateinit var teleports: Map<String, Teleport>

    init {
        load()
    }

    fun load() {
        teleports = load(files.load(path))
    }

    private fun load(array: Array<Teleport>): Map<String, Teleport> {
        val map = mutableMapOf<String, Teleport>()
        for (tele in array) {
            map["${tele.id}:${tele.tile}:${tele.option}"] = tele
        }
        return map
    }

    fun option(player: Player, obj: GameObject, option: String?) {
        val tele = teleports["${obj.id}:${obj.tile}:${option}"] ?: return
        player.action(ActionType.Climb) {
            if (obj.def.name.contains("ladder", true) || obj.def.name.contains("trapdoor", true)) {
                player.setAnimation(if (option == "Climb-down") 827 else 828)
                delay(2)
            }
            player.move(tele.delta)
            suspendCancellableCoroutine<Unit> { continuation ->
                player.movement.callback = {
                    continuation.resume(Unit)
                }
            }
            delay(1)
        }
    }


    private data class TeleportBuilder(
        val id: Int,
        val option: String,
        val tile: TeleTile,
        val delta: TeleTile
    ) {
        data class TeleTile(val x: Int, val y: Int, val plane: Int = 0)

        fun build() = Teleport(id, option, Tile(tile.x, tile.y, tile.plane), Delta(delta.x, delta.y, delta.plane))
    }

    @JsonDeserialize(builder = TeleportBuilder::class)
    private data class Teleport(val id: Int, val option: String, val tile: Tile, val delta: Delta)
}