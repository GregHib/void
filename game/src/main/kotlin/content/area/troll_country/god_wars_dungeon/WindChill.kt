package content.area.troll_country.god_wars_dungeon

import content.entity.combat.hit.directHit
import content.entity.player.effect.energy.runEnergy
import content.entity.sound.sound
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.event.Script
@Script
class WindChill {

    val areas: AreaDefinitions by inject()
    
    init {
        enterArea("godwars_chill_area") {
            player.sendVariable("godwars_knights_notes")
            player.timers.start("windchill")
        }

        exitArea("godwars_chill_area") {
            if (player.inventory.contains("knights_notes") || player.inventory.contains("knights_notes_opened")) {
                player["godwars_knights_notes"] = true
            }
            player.timers.stop("windchill")
        }

        timerStart("windchill") { player ->
            player.open("snow_flakes")
            interval = 10
        }

        timerTick("windchill") { player ->
            if (player.tile !in areas["godwars_chill_area"]) {
                cancel()
                return@timerTick
            }
            player.sound("windy")
            player.runEnergy = 0
            for (skill in Skill.all) {
                if (skill == Skill.Constitution) {
                    if (player.levels.get(Skill.Constitution) > 10) {
                        player.directHit(10)
                    }
                    continue
                }
                player.levels.drain(skill, 1)
            }
        }

        timerStop("windchill") { player ->
            player.close("snow_flakes")
        }

    }

}
