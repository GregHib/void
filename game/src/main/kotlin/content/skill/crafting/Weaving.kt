package content.skill.crafting

import content.entity.player.dialogue.type.makeAmount
import content.entity.player.dialogue.type.makeAmountIndex
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue

class Weaving : Script {

    val materials = listOf(
        Item("willow_branch"),
        Item("jute_fibre"),
        Item("flax"),
        Item("ball_of_wool"),
    )

    init {
        objectOperate("Weave", "loom_*", arrive = false) { (target) ->
            val strings = EnumDefinitions.get("weaving_product").map!!.values.filterIsInstance<String>()
            val (index, amount) = makeAmountIndex(
                items = strings,
                type = "Make",
                maximum = 28,
                text = "How many would you like to make?",
            )
            val item = materials[index]
            weave(target, item, amount)
        }

        itemOnObjectOperate(obj = "loom_*", arrive = false) { (target, item) ->
            val product = EnumDefinitions.stringOrNull("weaving_product", item.id) ?: return@itemOnObjectOperate
            val produced = EnumDefinitions.int("weaving_amount", item.id)
            val (_, amount) = makeAmount(
                items = listOf(product),
                type = "Make",
                maximum = inventory.count(item.id) / produced,
                text = "How many would you like to make?",
            )
            weave(target, item, amount)
        }
    }

    fun Player.weave(obj: GameObject, item: Item, amount: Int) {
        if (amount <= 0) {
            return
        }
        val current = inventory.count(item.id)
        val product = EnumDefinitions.string("weaving_product", item.id)
        val produced = EnumDefinitions.int("weaving_amount", item.id)
        if (current < produced) {
            val name = product.toLowerSpaceCase()
            message("You need $produced ${plural(item)} in order to make${name.an()} $name.")
            return
        }
        face(obj)
        val level = EnumDefinitions.int("weaving_level", item.id)
        if (!has(Skill.Crafting, level)) {
            return
        }
        anim("weaving")
        weakQueue("weave", 4) {
            inventory.transaction {
                remove(item.id, produced)
                add(product)
            }
            when (inventory.transaction.error) {
                is TransactionError.Full, is TransactionError.Deficient -> {
                    val name = product.toLowerSpaceCase()
                    message("You need $produced ${plural(item)} in order to make${name.an()} $name.")
                    return@weakQueue
                }
                else -> {}
            }
            val xp = EnumDefinitions.int("weaving_xp", item.id) / 10.0
            exp(Skill.Crafting, xp)
            weave(obj, item, amount - 1)
        }
    }

    fun plural(item: Item): String = when (item.id) {
        "willow_branch" -> "willow branches"
        "jute_fibre" -> "jute fibres"
        "flax" -> "flax"
        "ball_of_wool" -> "balls of wool"
        else -> item.id.plural()
    }
}
