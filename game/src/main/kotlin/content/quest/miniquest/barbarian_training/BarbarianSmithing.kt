package content.quest.miniquest.barbarian_training

import content.entity.player.dialogue.type.makeAmount
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

/**
 * Spears and hastae can only be forged on barbarian anvils, with a log of matching quality for the shaft.
 */
class BarbarianSmithing : Script {

    val shafts = mapOf(
        "bronze" to "logs",
        "iron" to "oak_logs",
        "steel" to "willow_logs",
        "mithril" to "maple_logs",
        "adamant" to "yew_logs",
        "rune" to "magic_logs",
    )

    init {
        itemOnObjectOperate("*_bar", "barbarian_anvil") { (_, item) ->
            val metal = item.id.removeSuffix("_bar")
            val logs = shafts[metal]
            if (logs == null) {
                // TODO confirm message
                message("You can't make anything from that metal on this anvil.")
                return@itemOnObjectOperate
            }
            if (!inventory.contains("hammer")) {
                statement("You need a hammer to work the metal with.")
                return@itemOnObjectOperate
            }
            val types = buildList {
                if (barbarianTrainingStarted("spear")) {
                    add("spear")
                }
                if (barbarianTrainingStarted("hasta")) {
                    add("hasta")
                }
            }
            if (types.isEmpty()) {
                statement(BarbarianTraining.NOT_STARTED_MESSAGE)
                return@itemOnObjectOperate
            }
            if (!inventory.contains(logs)) {
                // TODO confirm message
                statement("You need ${if (logs == "logs") "some normal logs" else "some ${logs.replace('_', ' ')}"} for the shaft.")
                return@itemOnObjectOperate
            }
            val maximum = minOf(inventory.count(item.id), inventory.count(logs))
            // TODO confirm the 2011 interface for choosing spear or hasta
            val (selected, amount) = makeAmount(types.map { "${metal}_$it" }, "Make", maximum)
            if (selected.isEmpty()) {
                return@itemOnObjectOperate
            }
            smith(selected, item.id, logs, amount)
        }

        itemOnObjectOperate("hammer", "barbarian_anvil", arrive = false) {
            // TODO confirm message
            message("To smith a spear, you must use the metal bar on the anvil.")
        }
    }

    suspend fun Player.smith(spear: String, bar: String, logs: String, amount: Int) {
        val row = Rows.getOrNull("smithing.$spear") ?: return
        val level = row.int("level")
        repeat(amount) {
            if (!has(Skill.Smithing, level, message = true)) {
                return
            }
            if (!inventory.contains("hammer")) {
                message("You need a Hammer to smith items.")
                return
            }
            // TODO confirm anim and smithing delay
            anim("smith_barbarian_anvil")
            delay(5)
            inventory.transaction {
                remove(bar)
                remove(logs)
                add(spear)
            }
            if (inventory.transaction.failed) {
                return
            }
            exp(Skill.Smithing, row.int("xp") / 10.0)
            // TODO confirm message
            message("You make a ${if (spear.endsWith("hasta")) "hasta" else "spear"}.", ChatType.Filter)
            barbarianTaskDone(if (spear.endsWith("hasta")) "hasta" else "spear")
        }
    }
}
