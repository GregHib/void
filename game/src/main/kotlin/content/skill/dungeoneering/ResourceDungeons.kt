package content.skill.dungeoneering

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound

class ResourceDungeons : Script {

    init {
        objectOperate("Enter") { (target) ->
            teleport(target.intId)
        }
        objectOperate("Exit") { (target) ->
            teleport(target.intId)
        }
    }

    private suspend fun world.gregs.voidps.engine.entity.character.player.Player.teleport(id: Int) {
        if (hasClock("resource_dungeon_teleporting")) {
            return
        }
        val dungeon = Rows.getOrNull("resource_dungeons.$id") ?: return
        val level = dungeon.int("level")
        if (!has(Skill.Dungeoneering, level)) {
            message("You need a Dungeoneering level of $level to venture in there.")
            return
        }
        start("resource_dungeon_teleporting", 5)
        anim("teleport_dungeoneering")
        sound("teleport")
        gfx("teleport_dungeoneering")
        delay(1)
        tele(dungeon.tile("destination"))
        anim("teleport_land_dungeoneering")
        sound("teleport_land")
        gfx("teleport_land_dungeoneering")
        sound("land_flatter", delay = 50)
        if (dungeon.bool("entering")) {
            val firstTimeKey = dungeon.string("first_time_key")
            if (!get(firstTimeKey, false)) {
                set(firstTimeKey, true)
                val xp = dungeon.int("xp")
                exp(Skill.Dungeoneering, xp.toDouble())
                message("You receive $xp Dungeoneering experience for uncovering a new dungeon for the first time.")
            }
        }
    }
}
