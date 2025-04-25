package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

/**
 * In osrs this is called an EngineQueue and also handles stat changes/level up:
 * it would check if the zone entered or exited contains a script and add them to this queue
 * to fire on the next cycle.
 *
 * However, this is simplified and looks up the areas directly and sends an event if one has changed
 */
class AreaQueue(
    private val player: Player
) {
    lateinit var areaDefinitions: AreaDefinitions

    fun tick() {
        if (player.steps.movedFrom.id == 0) {
            return
        }
        val from = player.steps.movedFrom
        player.steps.movedFrom = Tile.EMPTY
        player.emit(Moved(player, from, player.tile))
        val to = player.tile
        for (def in areaDefinitions.get(from.zone)) {
            if (from in def.area && to !in def.area) {
                player.emit(AreaExited(player, def.name, def.tags, def.area))
            }
        }
        for (def in areaDefinitions.get(to.zone)) {
            if (to in def.area && from !in def.area) {
                player.emit(AreaEntered(player, def.name, def.tags, def.area))
            }
        }
    }
}