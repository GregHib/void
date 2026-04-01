package content.area.misthalin.lumbridge.swamp.chams_of_tears

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile

class GuthixTemple : Script {
    init {
        objectApproach("Climb-up", "guthix_temple_wall_lower") {
            walkToDelay(Tile(2529, 5835))
            anim("guthix_temple_jump_up")
            delay(7)
            tele(2529, 5834, 1)
            clearAnim()
        }

        objectOperate("Jump-down", "guthix_temple_wall_down") { (target) ->
            walkToDelay(target.tile.addY(-1))
            anim("guthix_temple_jump_down")
            delay(3)
            tele(target.tile.copy(level = target.tile.level - 1))
            clearAnim()
        }

        objectApproach("Climb-up", "guthix_temple_wall_upper") {
            walkToDelay(Tile(2530, 5832, 1))
            anim("guthix_temple_jump_up")
            delay(7)
            tele(2530, 5832, 2)
            clearAnim()
        }

        objectOperate("Climb-through", "guthix_temple_cave_opening") { (target) ->
            walkToDelay(Tile(2527, 5830, 2))
            // TODO anim
            tele(2525, 5810, 0)
        }

        objectOperate("Crawl-through", "guthix_temple_granite_wall") { (target) ->
            walkToDelay(Tile(2525, 5810))
            tele(2527, 5830, 2)
        }
    }
}
