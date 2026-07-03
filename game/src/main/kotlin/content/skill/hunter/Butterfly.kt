package content.skill.hunter

import content.skill.melee.weapon.weapon
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class Butterfly : Script {
    init {
        itemOption("Release", "ruby_harvest,sapphire_glacialis,snowy_knight,black_warlock") { (item) ->
            anim("open_butterfly_jar")
            gfx("release_${item.id}")
            bonus(item.id)
            inventory.replace(item.id, "butterfly_jar")
            message("You release the ${item.id.toLowerSpaceCase()} butterfly from the jar.")
        }

        npcOperate("Catch", "ruby_harvest,sapphire_glacialis,snowy_knight,black_warlock") { (target) ->
            val butterfly = Rows.getOrNull("butterflies.${target.id}") ?: return@npcOperate
            val net = weapon.id == "butterfly_net" || weapon.id == "magic_butterfly_net"
            val level = if (net) butterfly.int("level") else butterfly.int("level_hands")
            if (!has(Skill.Hunter, level, message = if (net) " to catch this butterfly" else " to catch this butterfly barehanded")) { // TODO proper message
                return@npcOperate
            }
            if (!net && has(Skill.Agility, level - 5, message = " to catch this butterfly")) { // TODO proper message
                return@npcOperate
            }
            var jar = inventory.contains("butterfly_jar")
            if (net && !jar) {
                message("You do not have any empty jars to hold this butterfly with.") // TODO proper message
                return@npcOperate
            }
            anim("butterfly_catch")
            sound("butterfly_net")
            delay(2)
            var chance = butterfly.intRange("chance")
            if (weapon.id != "butterfly_net") { // Barehanded or magic net
                chance = (chance.first + 20.. chance.last + 20)
            }
            if (!Level.success(levels.get(Skill.Hunter), chance)) {
                return@npcOperate
            }
            if (jar) {
                inventory.remove("butterfly_jar")
                inventory.add(butterfly.item("jar"))
            }
            target.hide = true
            target.tele(target.get<Tile>("spawn_tile")!!)
            target.softTimers.start("reveal_butterfly")
            if (net) {
                exp(Skill.Hunter, butterfly.int("xp") / 10.0)
                message("You manage to catch the butterfly and place it in a jar.")
                return@npcOperate
            }
            if (!jar) {
                bonus(target.id)
                message("You catch and release the ruby harvest butterfly.")
            }
            exp(Skill.Hunter, butterfly.int("xp_hands") / 10.0)
        }

        npcTimerStart("reveal_butterfly") { TimeUnit.MINUTES.toTicks(Settings["hunter.butterfly.revealTicks", 2]) }

        npcTimerTick("reveal_butterfly") { Timer.CANCEL }

        npcTimerStop("reveal_butterfly") {
            hide = false
        }
    }

    private fun Player.bonus(id: String) {
        when (id) {
            "ruby_harvest" -> levels.boost(Skill.Attack, 4, 0.15)
            "sapphire_glacialis" -> levels.boost(Skill.Defence, 4, 0.15)
            "snowy_knight" -> levels.boost(Skill.Constitution, 150)
            "black_warlock" -> levels.boost(Skill.Strength, 4, 0.15)
        }
    }
}