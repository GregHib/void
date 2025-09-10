package content.area.troll_country.god_wars_dungeon

import content.entity.sound.areaSound
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

@Script
class GodwarsBoulder {

    init {
        objectOperate("Move", "godwars_boulder") {
            if (!player.has(Skill.Strength, 60, message = true)) { // TODO proper message
                return@objectOperate
            }
            val direction = if (player.tile.y < target.tile.y) Direction.NORTH else Direction.SOUTH
            player.walkToDelay(target.tile.addY(if (direction == Direction.NORTH) -1 else 3))
            player.face(direction)
            delay(2)
            player.anim("godwars_move_boulder${if (direction == Direction.NORTH) "" else "_reverse"}")
            delay(2)
            target.anim("godwars_boulder_move")
            areaSound("godwars_move_boulder", target.tile, radius = 5)
            delay(3)
            player.exactMoveDelay(target.tile.addY(if (direction == Direction.NORTH) 3 else -1), delay = 210, direction = direction)
            delay(2)
            areaSound("godwars_boulder_rollback", target.tile, radius = 5)
            target.anim("godwars_boulder_rollback")
        }

        objectOperate("Crawl-through", "godwars_little_hole") {
            if (!player.has(Skill.Agility, 60, message = true)) { // TODO proper message
                return@objectOperate
            }
            player.open("fade_out")
            delay(2)
            player.anim("godwars_human_crawling")
            delay(3)
            when (target.tile) {
                Tile(2900, 3713) -> player.tele(2904, 3720)
                Tile(2904, 3719) -> player.tele(2899, 3713)
            }
            player.open("fade_in")
        }
    }
}
