package content.area.troll_country.god_wars_dungeon

import content.entity.combat.hit.directHit
import content.entity.player.effect.energy.runEnergy
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.*

class WindChill : Script {

    val areas: AreaDefinitions by inject()

    init {
        timerStart("windchill") {
            open("snow_flakes")
            10
        }

        timerTick("windchill") {
            if (tile !in areas["godwars_chill_area"]) {
                return@timerTick Timer.CANCEL
            }
            sound("windy")
            runEnergy = 0
            for (skill in Skill.all) {
                if (skill == Skill.Constitution) {
                    if (levels.get(Skill.Constitution) > 10) {
                        directHit(10)
                    }
                    continue
                }
                levels.drain(skill, 1)
            }
            return@timerTick Timer.CONTINUE
        }

        timerStop("windchill") {
            close("snow_flakes")
        }

        entered("godwars_chill_area") {
            sendVariable("godwars_knights_notes")
            timers.start("windchill")
        }

        exited("godwars_chill_area") {
            if (inventory.contains("knights_notes") || inventory.contains("knights_notes_opened")) {
                set("godwars_knights_notes", true)
            }
            timers.stop("windchill")
        }
    }
}
