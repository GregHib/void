package content.skill.crafting

import content.entity.player.dialogue.type.makeAmount
import content.entity.player.dialogue.type.makeAmountIndex
import content.quest.quest
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue

class SpinningWheel : Script {

    val fibres = listOf(
        Item("wool"),
        Item("black_wool"),
        Item("golden_wool"),
        Item("flax"),
        Item("sinew"),
        Item("tree_roots"),
        Item("magic_roots"),
        Item("yak_hair"),
    )
    val treeRoots = listOf(
        Item("oak_roots"),
        Item("willow_roots"),
        Item("maple_roots"),
        Item("yew_roots"),
    )

    init {
        objectOperate("Spin", "spinning_wheel*", arrive = false) { (target) ->
            val availableFibres = fibres.filter { fibre ->
                (fibre.id != "black_wool" || quest("sheep_shearer_miniquest") == "started") && (fibre.id != "golden_wool" || (quest("fremennik_trials") == "started") || (quest("fremennik_trials") == "completed"))
            }
            val strings = availableFibres.map {
                if (it.id == "tree_roots") "crossbow_string" else Tables.item("spinning.${it.id}.product")
            }
            val (index, amount) = makeAmountIndex(
                items = strings,
                names = strings.mapIndexed { index, s ->
                    "${s.toSentenceCase()}<br>(${availableFibres[index].id.toSentenceCase()})"
                },
                type = "Make",
                maximum = 28,
                text = "How many would you like to make?",
            )

            delay()
            var item = fibres[index]
            if (item.id == "tree_roots") {
                val root = treeRoots.firstOrNull { inventory.contains(it.id) }
                if (root == null) {
                    message("You need some tree roots in order to make a crossbow string.")
                    return@objectOperate
                }
                item = root
            }
            val row = Tables.get("spinning").rows().firstOrNull { it.rowId == item.id } ?: return@objectOperate
            start(this, target, row, amount)
        }

        itemOnObjectOperate(obj = "spinning_wheel*", arrive = false) { (target, item) ->
            val rows = Tables.get("spinning").rows()
            val row = rows.firstOrNull { it.rowId == item.id } ?: return@itemOnObjectOperate
            val product = row.item("product")
            val (_, amount) = makeAmount(
                items = listOf(product),
                type = "Make",
                maximum = inventory.count(item.id),
                text = "How many would you like to make?",
            )
            start(this, target, row, amount)
        }
    }

    fun start(player: Player, obj: GameObject, row: RowDefinition, amount: Int) {
        val id = row.rowId
        val current = player.inventory.count(id)
        if (current <= 0) {
            val item = row.item("product")
            player.message("You need some ${id.toLowerSpaceCase()} in order to make a ${item.toLowerSpaceCase()}.")
            return
        }
        val actualAmount = if (current < amount) current else amount
        player.spin(obj, row, actualAmount)
    }

    fun Player.spin(obj: GameObject, row: RowDefinition, amount: Int) {
        val id = row.rowId
        if (amount <= 0) {
            return
        }
        val item = row.item("product")
        val current = inventory.count(id)
        if (current <= 0) {
            message("You need some ${id.toLowerSpaceCase()} in order to make a ${item.toLowerSpaceCase()}.")
            return
        }
        face(obj)
        val level = row.int("level")
        if (!has(Skill.Crafting, level)) {
            return
        }
        obj.anim("spinning_wheel")
        anim("spinning")
        sound("spinning")
        weakQueue("spin", 3) {
            if (!inventory.replace(id, item)) {
                message("You need some ${id.toLowerSpaceCase()} in order to make a ${item.toLowerSpaceCase()}.")
                return@weakQueue
            }
            val xp = row.int("xp") / 10.0
            exp(Skill.Crafting, xp)
            spin(obj, row, amount - 1)
        }
    }
}
