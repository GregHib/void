package content.quest.member.enakhras_lament

import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class EnakhrasTemple : Script {

    init {
        // The bones block the corridor south of the Camulet teleport spot; climbing moves to the other side.
        // https://runescape.wiki/w/Pile_of_bones
        npcOperate("Climb-over", "pile_of_bones_enakhras_temple_3") { (target) ->
            val bones = target.tile
            val size = target.def(this).size
            val north = tile.y >= bones.y + size / 2
            val x = tile.x.coerceIn(bones.x, bones.x + size - 1)
            val destination = if (north) Tile(x, bones.y - 1, bones.level) else Tile(x, bones.y + size, bones.level)
            val direction = if (north) Direction.SOUTH else Direction.NORTH
            face(direction)
            anim("rocks_pile_climb") // Guessed: no source for the 634 animation
            exactMoveDelay(destination, 60, direction)
        }
    }
}
