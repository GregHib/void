import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.transact.replace
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.definition.data.Spinning
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toSentenceCase
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.dialogue.type.makeAmountIndex

val fibres = listOf(
    Item("wool"),
    Item("golden_wool"),
    Item("flax"),
    Item("sinew"),
    Item("tree_roots"),
    Item("magic_roots"),
    Item("yak_hair")
)

val treeRoots = listOf(
    Item("oak_roots"),
    Item("willow_roots"),
    Item("maple_roots"),
    Item("yew_roots")
)

val Item.spinning: Spinning
    get() = def["spinning"]

on<ObjectOption>({ obj.id.startsWith("spinning_wheel") && option == "Spin" }) { player: Player ->
    player.dialogue {
        val strings = fibres.map { if (it.id == "tree_roots") "crossbow_string" else it.spinning!!.to }
        val (index, amount) = makeAmountIndex(
            items = strings,
            names = strings.mapIndexed { index, s ->
                "${s.toSentenceCase()}<br>(${fibres[index].id.toSentenceCase()})"
            },
            type = "Make",
            maximum = 28,
            text = "How many would you like to make?"
        )

        var fibre = fibres[index]
        if (fibre.id == "tree_roots") {
            val root = treeRoots.firstOrNull { player.inventory.contains(it.id) }
            if (root == null) {
                player.message("You need some tree roots in order to make a crossbow string.")
                return@dialogue
            }
            fibre = root
        }
        spin(player, obj, fibre, amount)
    }
}

on<InterfaceOnObject>({ obj.id.startsWith("spinning_wheel") && item.def.has("spinning") }) { player: Player ->
    player.dialogue {
        val (_, amount) = makeAmount(
            items = listOf(item.spinning.to),
            type = "Make",
            maximum = player.inventory.getCount(item.id).toInt(),
            text = "How many would you like to make?"
        )
        spin(player, obj, item, amount)
    }
}

fun spin(player: Player, obj: GameObject, fibre: Item, amount: Int) {
    val data = fibre.spinning
    val current = player.inventory.getCount(fibre.id).toInt()
    if (current <= 0) {
        player.message("You need some ${fibre.id.toLowerSpaceCase()} in order to make a ${data.to.toLowerSpaceCase()}.")
        return
    }
    val actualAmount = if (current < amount) current else amount
    player.face(obj)
    player.action(ActionType.Spinning) {
        if (actualAmount <= 0) {
            return@action
        }
        var tick = 0
        while (isActive && player.awaitDialogues() && tick < actualAmount) {
            if (!player.has(Skill.Crafting, data.level)) {
                return@action
            }
            player.setAnimation("spinning")
            delay(3)
            if (!player.inventory.replace(fibre.id, data.to)) {
                player.message("You need some ${fibre.id.toLowerSpaceCase()} in order to make a ${data.to.toLowerSpaceCase()}.")
                break
            }
            player.exp(Skill.Crafting, data.xp)
            tick++
        }
    }
}