package content.skill.cooking

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
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue

class DairyChurn : Script {

    val products = listOf("pot_of_cream", "pat_of_butter", "cheese")

    init {
        objectOperate("Churn", "dairy_churn*", arrive = false) { (target) ->
            if (!inventory.contains("bucket_of_milk") && !inventory.contains("pot_of_cream") && !inventory.contains("pat_of_butter")) {
                message("You need some milk, cream or butter to use in the churn.")
                return@objectOperate
            }
            val (index, amount) = makeAmountIndex(
                items = products,
                type = "Make",
                maximum = 28,
                text = "How many would you like to make?",
            )
            delay()
            val row = Tables.get("dairy").rows().firstOrNull { it.rowId == products.getOrNull(index) } ?: return@objectOperate
            churn(target, row, amount)
        }
    }

    fun Player.churn(obj: GameObject, row: RowDefinition, amount: Int) {
        if (amount <= 0) {
            return
        }
        val product = row.rowId
        val inputs = row.itemList("inputs")
        if (inputs.none { inventory.contains(it) }) {
            message("You need some milk, cream or butter to use in the churn.")
            return
        }
        val level = row.int("level")
        if (!has(Skill.Cooking, level)) {
            val name = product.toLowerSpaceCase()
            message("You need a Cooking level of $level to make${name.an()} $name.")
            return
        }
        face(obj)
        anim("churn")
        weakQueue("churn", 3) {
            val input = inputs.firstOrNull { inventory.contains(it) } ?: return@weakQueue
            if (!inventory.replace(input, product)) {
                return@weakQueue
            }
            if (input == "bucket_of_milk" && !inventory.add("bucket")) {
                FloorItems.add(tile, "bucket", disappearTicks = 300, owner = this)
            }
            exp(Skill.Cooking, row.int("xp") / 10.0)
            val name = product.toLowerSpaceCase()
            message("You make${name.an()} $name.")
            churn(obj, row, amount - 1)
        }
    }
}
