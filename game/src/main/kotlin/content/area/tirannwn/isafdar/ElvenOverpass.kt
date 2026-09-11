package content.area.tirannwn.isafdar

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class ElvenOverpass : Script {

    init {
        objectOperate("Climb", "elven_overpass_rocks_top,elven_overpass_rocks_bottom") { (target) ->
            val site = sites[target.tile] ?: return@objectOperate noInterest()
            if (!has(Skill.Agility, site.level)) {
                message("You need an Agility level of ${site.level} to negotiate these rocks.")
                return@objectOperate
            }
            if (target.id == "elven_overpass_rocks_top") {
                // Climb down facing the cliff
                anim("human_climbing_down", delay = site.animDelay)
                sound("climbing_loop", delay = 10, repeat = site.loops)
                exactMoveDelay(site.path.last(), delay = site.delay, direction = site.face)
                clearAnim()
            } else {
                // Climb up tile by tile
                renderEmote("climbing")
                sound("climbing_loop", repeat = site.loops)
                for (step in site.path) {
                    walkOverDelay(step)
                }
                clearRenderEmote()
            }
        }
    }

    private data class Site(val level: Int, val path: List<Tile>, val delay: Int = 0, val face: Direction = Direction.NONE, val animDelay: Int? = null, val loops: Int = 8)

    companion object {
        private val sites = mapOf(
            Tile(2346, 3299) to Site(59, listOf(Tile(2344, 3294)), delay = 120, face = Direction.NORTH, animDelay = 10, loops = 6),
            Tile(2344, 3295) to Site(59, listOf(Tile(2344, 3295), Tile(2344, 3296), Tile(2345, 3297), Tile(2345, 3298), Tile(2346, 3299), Tile(2346, 3300))),
            Tile(2338, 3282) to Site(68, listOf(Tile(2338, 3286)), delay = 90, face = Direction.SOUTH, loops = 4),
            Tile(2338, 3285) to Site(68, listOf(Tile(2338, 3285), Tile(2338, 3281))),
            Tile(2333, 3252) to Site(85, listOf(Tile(2338, 3253)), delay = 120, face = Direction.WEST, animDelay = 10, loops = 6),
            Tile(2337, 3253) to Site(85, listOf(Tile(2337, 3253), Tile(2332, 3252))),
        )
    }
}
