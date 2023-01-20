import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.client.ui.interact.ObjectInteraction
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.replace
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.definition.data.Spinning
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.suspend.delay
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
    val strings = fibres.map { if (it.id == "tree_roots") "crossbow_string" else it.spinning.to }
    val (index, amount) = makeAmountIndex(
        items = strings,
        names = strings.mapIndexed { index, s ->
            "${s.toSentenceCase()}<br>(${fibres[index].id.toSentenceCase()})"
        },
        type = "Make",
        maximum = 28,
        text = "How many would you like to make?"
    )

    delay()
    var fibre = fibres[index]
    if (fibre.id == "tree_roots") {
        val root = treeRoots.firstOrNull { player.inventory.contains(it.id) }
        if (root == null) {
            player.message("You need some tree roots in order to make a crossbow string.")
            return@on
        }
        fibre = root
    }
    spin(fibre, amount)
}

on<InterfaceOnObject>({ obj.id.startsWith("spinning_wheel") && item.def.has("spinning") }) { player: Player ->
    val (_, amount) = makeAmount(
        items = listOf(item.spinning.to),
        type = "Make",
        maximum = player.inventory.count(item.id),
        text = "How many would you like to make?"
    )
    spin(item, amount)
}

suspend fun ObjectInteraction.spin(fibre: Item, amount: Int) {
    val data = fibre.spinning
    val current = player.inventory.count(fibre.id)
    if (current <= 0) {
        player.message("You need some ${fibre.id.toLowerSpaceCase()} in order to make a ${data.to.toLowerSpaceCase()}.")
        return
    }
    val actualAmount = if (current < amount) current else amount
    player.face(obj)
    if (actualAmount <= 0) {
        return
    }
    var tick = 0
    while (player.awaitDialogues() && tick < actualAmount) {
        if (!player.has(Skill.Crafting, data.level)) {
            break
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