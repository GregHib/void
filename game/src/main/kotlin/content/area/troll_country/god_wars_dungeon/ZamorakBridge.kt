package content.area.troll_country.god_wars_dungeon

import content.entity.gfx.areaGfx
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction

class ZamorakBridge : Script {

    init {
        objectOperate("Climb-off", "godwars_zamorak_bridge") { (target) ->
            if (!has(Skill.Constitution, 700, message = true)) {
                return@objectOperate
            }
            val direction = if (tile.y <= target.tile.y) Direction.NORTH else Direction.SOUTH
            set("godwars_darkness", direction == Direction.NORTH)
            face(direction)
            walkToDelay(target.tile)
            delay()
            tele(target.tile.addY(direction.delta.y * 2))
            renderEmote("swim")
            areaGfx("big_splash", tile)
            delay(4)
            message("Dripping, you climb out of the water.")
            if (direction == Direction.NORTH) {
                levels.set(Skill.Prayer, 0)
                message("The extreme evil of this area leaves your Prayer drained.")
            }
            tele(target.tile.addY(direction.delta.y * 12))
            clearRenderEmote()
        }
    }
}
