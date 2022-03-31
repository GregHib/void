import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.data.Spinning
import world.gregs.voidps.engine.entity.item.Item
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

on<ObjectOption>({ obj.id.startsWith("spinning_wheel") && option == "Spin" }) { player: Player ->
    player.dialogue {
        val strings = fibres.map { if (it.id == "tree_roots") "crossbow_string" else it.def.get<Spinning>("spinning").to }
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
                player.message("") // TODO
                return@dialogue
            }
            fibre = root
        }
        spin(player, fibre, amount)
    }
}

on<InterfaceOnObject>({ obj.id.startsWith("spinning_wheel") && item.def.has("spinning") }) { player: Player ->
    player.dialogue {
        val (_, amount) = makeAmount(
            items = listOf(item.id),
            type = "Make",
            maximum = player.inventory.getCount(item.id).toInt(),
            text = "How many would you like to make?"
        )
        spin(player, item, amount)
    }
}

fun spin(player: Player, fibre: Item, amount: Int) {

}