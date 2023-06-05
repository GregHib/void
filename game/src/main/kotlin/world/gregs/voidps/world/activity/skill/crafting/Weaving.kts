package world.gregs.voidps.world.activity.skill.crafting

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.data.definition.data.Weaving
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.dialogue.type.makeAmountIndex

val materials = listOf(
    Item("willow_branch"),
    Item("jute_fibre"),
    Item("flax"),
    Item("ball_of_wool")
)

val Item.weaving: Weaving
    get() = def["weaving"]

on<ObjectOption>({ operate && obj.id.startsWith("loom_") && option == "Weave" }) { player: Player ->
    val strings = materials.map { it.weaving.to }
    val (index, amount) = makeAmountIndex(
        items = strings,
        type = "Make",
        maximum = 28,
        text = "How many would you like to make?"
    )
    val item = materials[index]
    weave(obj, item, amount)
}

on<InterfaceOnObject>({ operate && obj.id.startsWith("loom_") && item.def.has("weaving") }) { player: Player ->
    val (_, amount) = makeAmount(
        items = listOf(item.weaving.to),
        type = "Make",
        maximum = player.inventory.count(item.id) / item.weaving.amount,
        text = "How many would you like to make?"
    )
    weave(obj, item, amount)
}

fun PlayerContext.weave(obj: GameObject, item: Item, amount: Int) {
    val data = item.weaving
    val current = player.inventory.count(item.id)
    if (current < data.amount) {
        player.message("You need ${data.amount} ${plural(item)} in order to make ${form(data.to)} ${data.to.toLowerSpaceCase()}.")
        return
    }
    val actualAmount = if (current < amount * data.amount) current / data.amount else amount
    player.weave(obj, item, actualAmount)
}

fun Player.weave(obj: GameObject, item: Item, amount: Int) {
    if (amount <= 0) {
        return
    }
    val data = item.weaving
    val current = inventory.count(item.id)
    if (current < data.amount) {
        message("You need ${data.amount} ${plural(item)} in order to make ${form(data.to)} ${data.to.toLowerSpaceCase()}.")
        return
    }
    face(obj)
    if (!has(Skill.Crafting, data.level)) {
        return
    }
    setAnimation("weaving")
    weakQueue("weave", 4) {
        inventory.transaction {
            remove(item.id, data.amount)
            add(data.to)
        }
        when (inventory.transaction.error) {
            is TransactionError.Full, is TransactionError.Deficient -> {
                message("You need ${data.amount} ${plural(item)} in order to make ${form(data.to)} ${data.to.toLowerSpaceCase()}.")
                return@weakQueue
            }
            else -> {}
        }
        exp(Skill.Crafting, data.xp)
        weave(obj, item, amount - 1)
    }
}

fun plural(item: Item): String {
    return when (item.id) {
        "willow_branch" -> "willow branches"
        "jute_fibre" -> "jute fibres"
        "flax" -> "flax"
        "ball_of_wool" -> "balls of wool"
        else -> item.id.plural()
    }
}

fun form(item: String): String {
    return when (item) {
        "basket", "strip_of_cloth" -> "a"
        else -> "an"
    }
}