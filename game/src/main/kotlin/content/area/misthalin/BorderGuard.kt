package content.area.misthalin

import content.entity.obj.door.Door
import content.entity.obj.door.Door.isDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.type.Distance.nearestTo
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import kotlin.collections.set

class BorderGuard : Script {

    val guards = mutableMapOf<Rectangle, List<GameObject>>()

    val raised = mutableMapOf<GameObject, Boolean>()

    val gates = mutableMapOf<Rectangle, List<Tile>>()

    private val gateAutoCloseDelay = 5

    init {
        worldSpawn {
            for (border in Areas.tagged("border")) {
                val passage = border.area as Rectangle
                for (zone in passage.toZones()) {
                    val tiles = zone.toRectangle()
                    guards[passage] = tiles.mapNotNull {
                        val obj = GameObjects.getLayer(it, ObjectLayer.GROUND)
                        if (obj != null && obj.id.startsWith("border_guard")) obj else null
                    }
                    val doors = tiles.mapNotNull {
                        val obj = GameObjects.getLayer(it, ObjectLayer.WALL)
                        if (obj != null && obj.def.isDoor()) obj.tile else null
                    }
                    if (doors.isNotEmpty()) {
                        gates[passage] = doors
                    }
                }
            }
        }

        entered("border_guard_edgeville_varrock", ::enter)
        entered("border_guard_al_kharid_varrock", ::enter)
        entered("border_guard_port_sarim_draynor", ::enter)
        entered("border_guard_barbarian_village_varrock", ::enter)
        entered("border_guard_varrock_south", ::enter)
        entered("border_guard_draynor_barbarian_village", ::enter)
        entered("border_guard_draynor_falador", ::enter)
        entered("border_guard_varrock_east", ::enterGate)

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
        if (player.steps.destination in border || player.steps.isEmpty()) {
            val tile = border.nearestTo(player.tile)
            val endSide = Border.getOppositeSide(border, tile)
            player.walkTo(endSide, noCollision = true, forceWalk = true)
        } else {
            player.steps.update(noCollision = true, noRun = true)
        }
        val guards = guards[border] ?: return
        changeGuardState(guards, true)
    }

    fun exit(player: Player, def: AreaDefinition) {
        val border = def.area as Rectangle
        val guards = guards[border] ?: return
        player.steps.update(noCollision = false, noRun = false)
        changeGuardState(guards, false)
    }

    fun enterGate(player: Player, def: AreaDefinition) {
        val border = def.area as Rectangle
        for (gateTile in gates[border] ?: return) {
            val gate = GameObjects.getLayer(gateTile, ObjectLayer.WALL) ?: continue
            if (gate.id.endsWith("_closed")) {
                Door.openDoor(player, gate, ticks = gateAutoCloseDelay, collision = true)
            }
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
