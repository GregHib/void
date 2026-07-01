package content.skill.hunter

import content.quest.questCompleted
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Implings : Script {
    init {
        // TODO impling world map spawns

        npcOperate("Catch", "*_impling") { (target) ->
            val row = Rows.getOrNull("implings.${target.id}") ?: return@npcOperate
            val net = weapon.id == "butterfly_net" || weapon.id == "magic_butterfly_net"
            val level = if (net) row.int("level") else row.int("level") + 10
            if (!has(Skill.Hunter, level, message = if (net) "to catch this impling" else "to catch this impling barehanded")) { // TODO proper message
                return@npcOperate
            }
            if (target.id.startsWith("pirate_impling") && !questCompleted("rocking_out")) {
                message("You need to have completed Rocking Out to catch this impling.") // TODO proper message
                return@npcOperate
            }
            if (net && !inventory.contains("impling_jar")) {
                message("You do not have any empty jars to hold this impling with.") // TODO proper message
                return@npcOperate
            }
            anim("butterfly_catch")
            delay(2)
            if (!Level.success(levels.get(Skill.Hunter), 1..1)) { // TODO chances
                message("You fail to catch the impling!") // TODO proper message
                return@npcOperate
            }
            NPCs.remove(target)
            inventory.remove("impling_jar")
            inventory.add(row.item("jar"))
            if (tile in Areas["puro_puro"]) {
                exp(Skill.Hunter, row.int("xp_puro") / 10.0)
            } else {
                exp(Skill.Hunter, row.int("xp") / 10.0)
            }
            message("You catch the impling!") // TODO proper message
        }
    }

}