package content.entity.player.effect

import content.skill.prayer.praying
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.levelChange
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit

@Script
class HitpointRestoration {

    init {
        playerSpawn { player ->
            if (player.levels.getOffset(Skill.Constitution) < 0) {
                player.softTimers.start("restore_hitpoints")
            }
        }

        levelChange(Skill.Constitution) { player ->
            if (to <= 0 || to >= player.levels.getMax(skill) || player.softTimers.contains("restore_hitpoints")) {
                return@levelChange
            }
            player.softTimers.start("restore_hitpoints")
        }

        timerStart("restore_hitpoints") {
            interval = TimeUnit.SECONDS.toTicks(6)
        }

        timerTick("restore_hitpoints") { player ->
            if (player.levels.get(Skill.Constitution) == 0) {
                cancel()
                return@timerTick
            }
            val total = player.levels.restore(Skill.Constitution, healAmount(player))
            if (total == 0) {
                cancel()
            }
        }
    }

    /**
     * Default heal 20 every 12s for 100hp per min.
     */
    fun healAmount(player: Player): Int {
        var heal = 1
        val movement = player["movement", "walk"]
        if (movement == "rest") {
            heal += 1
        } else if (movement == "music") {
            heal += 2
        } else if (player["dream", false]) {
            heal += 4
        }
        if (player.praying("rapid_renewal")) {
            heal += 5
        } else if (player.praying("rapid_heal") || player.equipped(EquipSlot.Cape).id.startsWith("constitution_cape")) {
            heal += 1
        }
        if (player.equipped(EquipSlot.Hands).id == "regen_bracelet") {
            heal *= 2
        }
        return heal
    }
}
