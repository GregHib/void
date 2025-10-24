package content.area.troll_country.god_wars_dungeon

import content.entity.combat.hit.directHit
import content.entity.player.effect.energy.runEnergy
import content.entity.sound.sound
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.*

@Script
class WindChill : Api {

    val areas: AreaDefinitions by inject()

    @Key("windchill")
    override fun start(player: Player, timer: String, restart: Boolean): Int {
        player.open("snow_flakes")
        return 10
    }

    @Key("windchill")
    override fun tick(player: Player, timer: String): Int {
        if (player.tile !in areas["godwars_chill_area"]) {
            return Timer.CANCEL
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
        return Timer.CONTINUE
    }

    @Key("windchill")
    override fun stop(player: Player, timer: String, logout: Boolean) {
        player.close("snow_flakes")
    }

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
    }
}
