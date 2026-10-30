package content.area.tirannwn.isafdar

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Direction

class ElvenOverpass : Script {

    init {
        objectOperate("Climb", "elven_overpass_rocks_top,elven_overpass_rocks_bottom") { (target) ->
            val site = Tables.get("elven_overpass").rows().firstOrNull { it.tile("tile") == target.tile } ?: return@objectOperate noInterest()
            val level = site.int("level")
            if (!has(Skill.Agility, level)) {
                message("You need an Agility level of $level to negotiate these rocks.")
                return@objectOperate
            }
            val path = site.tileList("path")
            if (target.id == "elven_overpass_rocks_top") {
                // Climb down facing the cliff
                anim("human_climbing_down", delay = site.int("anim_delay").takeIf { it > 0 })
                sound("climbing_loop", delay = 10, repeat = site.int("loops"))
                exactMoveDelay(path.last(), delay = site.int("delay"), direction = Direction.valueOf(site.string("face").uppercase()))
                clearAnim()
            } else {
                // Climb up tile by tile
                renderEmote("climbing")
                sound("climbing_loop", repeat = site.int("loops"))
                for (step in path) {
                    walkOverDelay(step)
                }
                clearRenderEmote()
            }
        }
    }
}
