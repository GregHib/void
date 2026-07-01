package content.skill.hunter

import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Butterflies : Script {
    init {
        // TODO butterfly jar bonuses
//            message("You release the ruby harvest butterfly from the jar.")
//        message("You break the jar as you try to open it. You throw the shattered remains away.", ChatType.Filter)

        npcOperate("Catch", "ruby_harvest,sapphire_glacialis,snowy_knight,black_warlock") { (target) ->
            val butterfly = Rows.getOrNull("butterflies.${target.id}") ?: return@npcOperate
            val net = weapon.id == "butterfly_net" || weapon.id == "magic_butterfly_net"
            val level = if (net) butterfly.int("level") else butterfly.int("level_hands")
            if (!has(Skill.Hunter, level, message = if (net) "to catch this butterfly" else "to catch this butterfly barehanded")) { // TODO proper message
                return@npcOperate
            }
            if (!net && has(Skill.Agility, level - 5, message = "to catch this butterfly")) { // TODO proper message
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
            if (!Level.success(levels.get(Skill.Hunter), 1..1)) {// TODO chances
                message("You failed to catch the butterfly.", ChatType.Filter) // TODO proper message
                return@npcOperate
            }
            if (jar) {
                inventory.remove("butterfly_jar")
                inventory.add(butterfly.item("jar"))
            }
            target.hide = true
            target.levels.set(Skill.Constitution, 0)
            if (net) {
                exp(Skill.Hunter, butterfly.int("xp") / 10.0)
                message("You manage to catch the butterfly and place it in a jar.")
                return@npcOperate
            }
            if (!jar) {
                // TODO effect
                message("You catch and release the ruby harvest butterfly.")
            }
            exp(Skill.Hunter, butterfly.int("xp_hands") / 10.0)
        }
    }
}