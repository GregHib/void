package content.skill.crafting

import content.entity.player.dialogue.type.makeAmount
import content.entity.player.dialogue.type.makeAmountIndex
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue

class Weaving : Script {

    init {
        objectOperate("Weave", "loom_*", arrive = false) { (target) ->
            val rows = Tables.get("weaving").rows()
            val strings = rows.map { it.stringId }
            val (index, amount) = makeAmountIndex(
                items = strings,
                type = "Make",
                maximum = 28,
                text = "How many would you like to make?",
            )
            val row = rows[index]
            weave(target, row, amount)
        }

        itemOnObjectOperate(obj = "loom_*", arrive = false) { (target, item) ->
            val rows = Tables.get("weaving").rows()
            val row = rows.firstOrNull { it.rowId == item.id } ?: return@itemOnObjectOperate
            val product = row.item("product")
            val produced = row.int("amount")
            val (_, amount) = makeAmount(
                items = listOf(product),
                type = "Make",
                maximum = inventory.count(item.id) / produced,
                text = "How many would you like to make?",
            )
            weave(target, row, amount)
        }
    }

    fun Player.weave(obj: GameObject, row: RowDefinition, amount: Int) {
        if (amount <= 0) {
            return
        }
        val current = inventory.count(row.rowId)
        val product = row.item("product")
        val produced = row.int("amount")
        val plural = row.string("plural")
        if (current < produced) {
            val name = product.toLowerSpaceCase()
            message("You need $produced $plural in order to make${name.an()} $name.")
            return
        }
        face(obj)
        val level = row.int("level")
        if (!has(Skill.Crafting, level)) {
            return
        }
        anim("weaving")
        weakQueue("weave", 4) {
            inventory.transaction {
                remove(row.rowId, produced)
                add(product)
            }
            when (inventory.transaction.error) {
                is TransactionError.Full, is TransactionError.Deficient -> {
                    val name = product.toLowerSpaceCase()
                    message("You need $produced $plural in order to make${name.an()} $name.")
                    return@weakQueue
                }
                else -> {}
            }
            val xp = row.int("xp") / 10.0
            exp(Skill.Crafting, xp)
            weave(obj, row, amount - 1)
        }
    }
}
