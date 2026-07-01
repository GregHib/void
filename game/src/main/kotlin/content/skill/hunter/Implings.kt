package content.skill.hunter

import content.entity.player.dialogue.type.item
import content.quest.questCompleted
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.random

class Implings : Script {
    init {
        // TODO impling world map spawns
        // https://youtu.be/O5_IjnlYXrU?&t=165
        // The imp tried to steal one of your implings, but you stopped him!
        // You use your strength to push through the wheat in the most efficient fashion.
        // You use your strength to push through the wheat.
        // You push through the wheat. It's hard work, though.


        npcOperate("Catch", "*_impling") { (target) ->
            val row = Rows.getOrNull("implings.${target.id}") ?: return@npcOperate
            val net = weapon.id == "butterfly_net" || weapon.id == "magic_butterfly_net"
            val level = if (net) row.int("level") else row.int("level") + 10
            if (!has(Skill.Hunter, level, message = if (net) "to catch this impling" else "to catch this impling barehanded")) { // TODO proper message
                return@npcOperate
            }
            val puroPuro = tile in Areas["puro_puro"]
            if (target.id.startsWith("pirate_impling") && !questCompleted("rocking_out")) {
                message("You need to have completed Rocking Out to catch this impling.") // TODO proper message
                return@npcOperate
            }
            if (puroPuro && !inventory.contains("impling_jar")) {
                message("You do not have an empty impling jar in which to keep an impling.")
                return@npcOperate
            }
            anim("butterfly_catch")
            delay(2)
            if (!Level.success(levels.get(Skill.Hunter), 1..1)) { // TODO chances
                target.mode = Retreat(target, this)
                message("You fail to catch the impling!") // TODO proper message
                return@npcOperate
            }
            target.hide = true
            target.levels.set(Skill.Constitution, 0)
            if (random.nextInt(1000) == 0) {
                // TODO charms
                // https://youtu.be/O5_IjnlYXrU?&t=129
                message("You manage to catch the impling. It drops charms and flies away.")
                item("blue_charm", "The impling was carrying a blue charm.")
            }

            // TODO if not in puro-puro and no jars -> loot
//            message("You manage to catch the impling and acquire some loot.")
            inventory.remove("impling_jar")
            inventory.add(row.item("jar"))
            if (puroPuro) {
                exp(Skill.Hunter, row.int("xp_puro") / 10.0)
            } else {
                exp(Skill.Hunter, row.int("xp") / 10.0)
            }
            message("You manage to catch the impling and squeeze it into a jar.", ChatType.Filter)
        }
    }

}