package content.area.tirannwn.isafdar

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.chat.obstacle
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.type.Tile

class ElvenOverpass : Script {

    init {
        objectOperate("Climb", "elven_overpass_rocks_up,elven_overpass_rocks_down") { (target) ->
            val up = target.id == "elven_overpass_rocks_up"
            val (level, dest) = when (target.tile) {
                Tile(2333, 3252) -> 85 to Tile(2338, 3253)
                Tile(2338, 3282) -> 68 to Tile(2338, 3286)
                Tile(2346, 3299) -> 59 to Tile(2344, 3294)
                Tile(2337, 3253) -> 85 to Tile(2332, 3252)
                Tile(2338, 3285) -> 68 to Tile(2338, 3281)
                Tile(2344, 3295) -> 59 to Tile(2346, 3300)
                else -> return@objectOperate noInterest()
            }
            if (!has(Skill.Agility, level)) {
                obstacle(level)
                return@objectOperate
            }
            anim(if (up) "elven_overpass_climb_up" else "elven_overpass_climb_down")
            exactMoveDelay(dest, delay = 90)
        }
    }
}
