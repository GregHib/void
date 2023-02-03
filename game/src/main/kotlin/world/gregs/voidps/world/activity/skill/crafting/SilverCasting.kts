import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.chat.Green
import world.gregs.voidps.engine.client.ui.chat.Orange
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.replace
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.definition.data.Silver
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.activity.quest.started
import world.gregs.voidps.world.interact.dialogue.type.intEntry

val moulds = listOf(
    Item("holy_mould"),
    Item("sickle_mould"),
    Item("tiara_mould"),
    Item("demonic_sigil_mould"),
    Item("chain_link_mould"),
    Item("unholy_mould"),
    Item("conductor_mould"),
    Item("rod_clay_mould"),
    Item("bolt_mould"),
    Item("key_mould")
)

val Item.silver: Silver?
    get() = def.getOrNull("silver_jewellery")

on<InterfaceOpened>({ id == "silver_mould" }) { player: Player ->
    for (mould in moulds) {
        val silver = mould.silver ?: continue
        val item = silver.item
        val quest = silver.quest
        player.interfaces.sendVisibility(id, mould.id, quest == null || player.started(quest))
        val has = player.hasItem(mould.id)
        val colour = if (has && player.hasItem("silver_bar")) Green else Orange
        player.interfaces.sendText(id,
            "${mould.id}_text",
            colour.wrap(if (has) "Make ${item.def.name.toTitleCase()}" else "You need a ${silver.name ?: mould.def.name.lowercase()} to make this item."))
        player.interfaces.sendItem(id, "${mould.id}_model", if (has) item else mould)
    }
}

on<InterfaceOnObject>({ obj.id.startsWith("furnace") && item.id == "silver_bar" }) { player: Player ->
    player.open("silver_mould")
}

on<InterfaceOnObject>({ obj.id.startsWith("furnace") && item.silver != null }) { player: Player ->
    player.make(item, 1)
}

on<InterfaceOption>({ id == "silver_mould" && component.endsWith("_button") }) { player: Player ->
    val amount = when (option) {
        "Make 1" -> 1
        "Make 5" -> 5
        "Make All" -> 28
        else -> return@on
    }
    player.make(Item(component.removeSuffix("_button")), amount)
}

on<InterfaceOption>({ id == "trade_side" && component.endsWith("_button") && option == "Offer-X" }) { player: Player ->
    val amount = intEntry("Enter amount:")
    player.make(Item(component.removeSuffix("_button")), amount)
}

fun Player.make(item: Item, amount: Int) {
    if (amount <= 0) {
        return
    }
    val data = item.silver ?: return
    closeInterface()
    if (!inventory.contains(item.id)) {
        message("You need a ${item.def.name} in order to make a ${data.item.def.name}.")
        return
    }
    if (!inventory.contains("silver_bar")) {
        message("You need a silver bar in order to make a ${data.item.def.name}.")
        return
    }
    if (!has(Skill.Crafting, data.level)) {
        return
    }
    if (!inventory.contains("silver_bar")) {
        message("You have run out of silver bars to make another ${data.item.def.name}.")
        return
    }
    setAnimation("cook_range")
    weakQueue(3) {
        inventory.replace("silver_bar", data.item.id)
        exp(Skill.Crafting, data.xp)
        make(item, amount - 1)
    }
}