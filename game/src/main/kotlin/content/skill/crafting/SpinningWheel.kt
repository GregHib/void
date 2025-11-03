package content.skill.crafting

import content.entity.player.dialogue.type.makeAmount
import content.entity.player.dialogue.type.makeAmountIndex
import content.entity.sound.sound
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.data.Spinning
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue

class SpinningWheel : Script {

    val fibres = listOf(
        Item("wool"),
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

    val Item.spinning: Spinning
        get() = def["spinning"]

    init {
        objectOperate("Spin", "spinning_wheel*", arrive = false) { (target) ->
            val strings = fibres.map { if (it.id == "tree_roots") "crossbow_string" else it.spinning.to }
            val (index, amount) = makeAmountIndex(
                items = strings,
                names = strings.mapIndexed { index, s ->
                    "${s.toSentenceCase()}<br>(${fibres[index].id.toSentenceCase()})"
                },
                type = "Make",
                maximum = 28,
                text = "How many would you like to make?",
            )

            delay()
            var fibre = fibres[index]
            if (fibre.id == "tree_roots") {
                val root = treeRoots.firstOrNull { inventory.contains(it.id) }
                if (root == null) {
                    message("You need some tree roots in order to make a crossbow string.")
                    return@objectOperate
                }
                fibre = root
            }
            start(this, target, fibre, amount)
        }

        itemOnObjectOperate(obj = "spinning_wheel*", arrive = false) { (target, item) ->
            if (!item.def.contains("spinning")) {
                return@itemOnObjectOperate
            }
            val (_, amount) = makeAmount(
                items = listOf(item.spinning.to),
                type = "Make",
                maximum = inventory.count(item.id),
                text = "How many would you like to make?",
            )
            start(this, target, item, amount)
        }
    }

    fun start(player: Player, obj: GameObject, fibre: Item, amount: Int) {
        val data = fibre.spinning
        val current = player.inventory.count(fibre.id)
        if (current <= 0) {
            player.message("You need some ${fibre.id.toLowerSpaceCase()} in order to make a ${data.to.toLowerSpaceCase()}.")
            return
        }
        val actualAmount = if (current < amount) current else amount
        player.spin(obj, fibre, actualAmount)
    }

    fun Player.spin(obj: GameObject, fibre: Item, amount: Int) {
        if (amount <= 0) {
            return
        }
        val data = fibre.spinning
        val current = inventory.count(fibre.id)
        if (current <= 0) {
            message("You need some ${fibre.id.toLowerSpaceCase()} in order to make a ${data.to.toLowerSpaceCase()}.")
            return
        }
        face(obj)
        if (!has(Skill.Crafting, data.level)) {
            return
        }
        obj.anim("spinning_wheel")
        anim("spinning")
        sound("spinning")
        weakQueue("spin", 3) {
            if (!inventory.replace(fibre.id, data.to)) {
                message("You need some ${fibre.id.toLowerSpaceCase()} in order to make a ${data.to.toLowerSpaceCase()}.")
                return@weakQueue
            }
            exp(Skill.Crafting, data.xp)
            spin(obj, fibre, amount - 1)
        }
    }
}
