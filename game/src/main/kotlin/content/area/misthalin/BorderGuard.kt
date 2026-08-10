package content.area.misthalin

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.type.area.Rectangle
import kotlin.collections.set

class BorderGuard : Script {

    val guards = mutableMapOf<Rectangle, List<GameObject>>()

    init {
        worldSpawn {
            val table = Tables.getOrNull("guards") ?: return@worldSpawn
            for (row in table.rows()) {
                val id = row.obj("obj")
                val tiles = row.tileList("tiles")
                val passage = Areas[row.rowId] as Rectangle
                guards[passage] = tiles.map { tile -> GameObjects.findLayer(tile, ObjectLayer.GROUND, id) }
            }
        }

        entered("border_guard_edgeville_varrock", ::enter)
        entered("border_guard_al_kharid_varrock", ::enter)
        entered("border_guard_port_sarim_draynor", ::enter)
        entered("border_guard_barbarian_village_varrock", ::enter)
        entered("border_guard_varrock_south", ::enter)
        entered("border_guard_draynor_barbarian_village", ::enter)
        entered("border_guard_draynor_falador", ::enter)

        exited("border_guard_edgeville_varrock", ::exit)
        exited("border_guard_al_kharid_varrock", ::exit)
        exited("border_guard_port_sarim_draynor", ::exit)
        exited("border_guard_barbarian_village_varrock", ::exit)
        exited("border_guard_varrock_south", ::exit)
        exited("border_guard_draynor_barbarian_village", ::exit)
        exited("border_guard_draynor_falador", ::exit)
    }

    fun enter(player: Player, def: AreaDefinition) {
        val border = def.area as Rectangle
        player["delay"] = 3
        player.steps.update(noCollision = true, noRun = true)
        val guards = guards[border] ?: return
        changeGuardState(guards)
    }

    fun exit(player: Player, def: AreaDefinition) {
        player.steps.update(noCollision = false, noRun = false)
    }

    fun changeGuardState(guards: List<GameObject>) {
        for (guard in guards) {
            if (World.containsQueue("${guard.id}_${guard.tile}")) {
                continue
            }
            guard.anim(guard.def["raise"])
            World.queue("${guard.id}_${guard.tile}", 3) {
                guard.anim(guard.def["lower"])
            }
        }
    }
}
