package content.area.troll_country.god_wars_dungeon

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class GodwarsBoulder : Script {

    init {
        objectOperate("Move", "godwars_boulder") { (target) ->
            if (!has(Skill.Strength, 60, message = false)) {
                message("You need a Strength level of 60 to negotiate these rocks.")
                return@objectOperate
            }
            val direction = if (tile.y < target.tile.y) Direction.NORTH else Direction.SOUTH
            walkToDelay(target.tile.addY(if (direction == Direction.NORTH) -1 else 3))
            face(direction)
            delay(2)
            anim("godwars_move_boulder${if (direction == Direction.NORTH) "" else "_reverse"}")
            delay(2)
            target.anim("godwars_boulder_move")
            areaSound("godwars_move_boulder", target.tile, radius = 5)
            delay(3)
            exactMoveDelay(target.tile.addY(if (direction == Direction.NORTH) 3 else -1), delay = 210, direction = direction)
            delay(2)
            areaSound("godwars_boulder_rollback", target.tile, radius = 5)
            target.anim("godwars_boulder_rollback")
        }

        objectOperate("Crawl-through", "godwars_little_hole") { (target) ->
            if (!has(Skill.Agility, 60, message = false)) {
                message("You need an Agility level of 60 to squeeze through the crack.")
                return@objectOperate
            }
            open("fade_out")
            delay(2)
            anim("godwars_human_crawling")
            delay(3)
            when (target.tile) {
                Tile(2900, 3713) -> tele(2904, 3720)
                Tile(2904, 3719) -> tele(2899, 3713)
            }
            open("fade_in")
        }
    }
}
