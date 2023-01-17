import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.chat.Green
import world.gregs.voidps.engine.client.ui.chat.Orange
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
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
        player.interfaces.sendText(id, "${mould.id}_text", colour.wrap(if (has) "Make ${item.def.name.toTitleCase()}" else "You need a ${silver.name ?: mould.def.name.lowercase()} to make this item."))
        player.interfaces.sendItem(id, "${mould.id}_model", if (has) item else mould)
    }
}

on<InterfaceOnObject>({ obj.id.startsWith("furnace") && item.id == "silver_bar" }) { player: Player ->
    player.open("silver_mould")
}

on<InterfaceOnObject>({ obj.id.startsWith("furnace") && item.silver != null }) { player: Player ->
    make(player, item, 1)
}

on<InterfaceOption>({ id == "silver_mould" && component.endsWith("_button") }) { player: Player ->
    val amount = when (option) {
        "Make 1" -> 1
        "Make 5" -> 5
        "Make All" -> 28
        else -> return@on
    }
    make(player, Item(component.removeSuffix("_button")), amount)
}

on<InterfaceOption>({ id == "trade_side" && component.endsWith("_button") && option == "Offer-X" }) { player: Player ->
    player.dialogue {
        val amount = intEntry("Enter amount:")
        make(player, Item(component.removeSuffix("_button")), amount)
    }
}

fun make(player: Player, item: Item, amount: Int) {
    val data = item.silver ?: return
    player.action(ActionType.Making) {
        player.closeInterface()
        var tick = 0
        if (!player.inventory.contains(item.id)) {
            player.message("You need a ${item.def.name} in order to make a ${data.item.def.name}.")
            return@action
        }
        if (!player.inventory.contains("silver_bar")) {
            player.message("You need a silver bar in order to make a ${data.item.def.name}.")
            return@action
        }
        while (isActive && player.awaitDialogues() && tick < amount) {
            if (!player.has(Skill.Crafting, data.level)) {
                break
            }
            if (!player.inventory.contains("silver_bar")) {
                player.message("You have run out of silver bars to make another ${data.item.def.name}.")
                break
            }
            player.setAnimation("cook_range")
            delay(3)
            player.inventory.replace("silver_bar", data.item.id)
            player.exp(Skill.Crafting, data.xp)
            tick++
        }
    }
}