package content.area.misthalin

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Distance.nearestTo
import world.gregs.voidps.type.area.Rectangle
import kotlin.collections.set

class BorderGuard : Script {

    val objects: GameObjects by inject()
    val areas: AreaDefinitions by inject()

    val guards = mutableMapOf<Rectangle, List<GameObject>>()

    val raised = mutableMapOf<GameObject, Boolean>()

    init {
        worldSpawn {
            for (border in areas.getTagged("border")) {
                val passage = border.area as Rectangle
                for (zone in passage.toZones()) {
                    guards[passage] = zone.toRectangle().mapNotNull {
                        val obj = objects.getLayer(it, ObjectLayer.GROUND)
                        if (obj != null && obj.id.startsWith("border_guard")) obj else null
                    }
                }
            }
        }

        entered("border_guard*") { area ->
            val border = area as Rectangle
            if (steps.destination in border || steps.isEmpty()) {
                val tile = border.nearestTo(tile)
                val endSide = Border.getOppositeSide(border, tile)
                walkTo(endSide, noCollision = true, forceWalk = true)
            } else {
                steps.update(noCollision = true, noRun = true)
            }
            val guards = guards[border] ?: return@entered
            changeGuardState(guards, true)
        }

        exited("border_guard*") { area ->
            val border = area as Rectangle
            val guards = guards[border] ?: return@exited
            steps.update(noCollision = false, noRun = false)
            changeGuardState(guards, false)
        }
    }

    fun changeGuardState(guards: List<GameObject>, raise: Boolean) {
        for (guard in guards) {
            if (raised.getOrDefault(guard, false) != raise) {
                guard.anim(guard.def[if (raise) "raise" else "lower"])
                raised[guard] = raise
            }
        }
    }
}
