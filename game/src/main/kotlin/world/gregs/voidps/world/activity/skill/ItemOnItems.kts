package world.gregs.voidps.world.activity.skill

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.data.definition.ItemOnItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.Transaction
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.entity.combat.inCombat
import world.gregs.voidps.world.interact.entity.sound.playSound

val itemOnItemDefs: ItemOnItemDefinitions by inject()

itemOnItem { player ->
    val overlaps = itemOnItemDefs.getOrNull(fromItem, toItem) ?: return@itemOnItem
    if (overlaps.isEmpty()) {
        return@itemOnItem
    }
    player.closeInterfaces()
    player.weakQueue("item_on_item") {
        player.softTimers.start("item_on_item")
        val maximum = getMaximum(overlaps, player)
        val (def, amount) = if (makeImmediately(player, overlaps, maximum, player.inventory)) {
            player.closeDialogue()
            overlaps.first() to 1
        } else {
            val definition = overlaps.first()
            val type = definition.type
            val (selection, amount) = makeAmount(
                overlaps.map { it.add.first().id }.distinct().toList(),
                type = type.toSentenceCase(),
                maximum = maximum,
                text = definition.question
            )
            overlaps.first { it.add.first().id == selection } to amount
        }
        useItemOnItem(player, def.skill, def, amount, 0)
    }
}

fun useItemOnItem(
    player: Player,
    skill: Skill?,
    def: ItemOnItemDefinition,
    amount: Int,
    count: Int
) {
    if (count >= amount) {
        player.softTimers.stop("item_on_item")
        return
    }

    if (skill != null && !player.has(skill, def.level, true)) {
        player.softTimers.stop("item_on_item")
        return
    }

    val transaction = player.inventory.transaction
    transaction.start()
    val message = transaction.removeItems(def, success = true)
    if (!transaction.revert()) {
        player.message(message)
        player.softTimers.stop("item_on_item")
        return
    }
    if (transaction.failed) {
        player.message(message)
        player.softTimers.stop("item_on_item")
        return
    }
    if (def.animation.isNotEmpty()) {
        player.setAnimation(def.animation)
    }
    if (def.graphic.isNotEmpty()) {
        player.setGraphic(def.graphic)
    }
    if (def.sound.isNotEmpty()) {
        player.playSound(def.sound)
    }
    player.weakQueue("item_on_item_delay", if (count == 0) def.delay else def.ticks) {
        replaceItems(def, player, skill, amount, count)
    }
}

fun replaceItems(
    def: ItemOnItemDefinition,
    player: Player,
    skill: Skill?,
    amount: Int,
    count: Int
) {
    val success = skill == null || Level.success(player.levels.get(skill), def.chance)
    val transaction = player.inventory.transaction
    val message = transaction.removeItems(def, success)
    if (!transaction.commit()) {
        player.message(message)
        player.softTimers.stop("item_on_item")
        return
    }
    if (success) {
        if (def.message.isNotEmpty()) {
            player.message(def.message, ChatType.Filter)
        }
        if (skill != null) {
            player.exp(skill, def.xp)
        }
        player.emit(ItemUsedOnItem(def))
    } else {
        if (def.failure.isNotEmpty()) {
            player.message(def.failure, ChatType.Filter)
        }
    }
    useItemOnItem(player, skill, def, amount, count + 1)
}

interfaceClose("dialogue_skill_creation") { player ->
    player.clear("selecting_amount")
}

interfaceOpen("dialogue_skill_creation") { player ->
    player["selecting_amount"] = true
}

fun makeImmediately(player: Player, overlaps: List<ItemOnItemDefinition>, maximum: Int, inventory: Inventory): Boolean {
    if (overlaps.size != 1) {
        return false
    }
    val definition = overlaps.first()
    val stackable = definition.maximum == -1 && definition.remove.all { inventory.stackable(it.id) } && definition.one.all { inventory.stackable(it.id) }
    return stackable || maximum == 1 || player["selecting_amount", false] || player.inCombat
}

fun getMaximum(overlaps: List<ItemOnItemDefinition>, player: Player): Int {
    var max = 0
    for (overlap in overlaps) {
        val min = overlap.remove.distinct().minOf { item ->
            val count = player.inventory.count(item.id)
            val required = overlap.remove.filter { it.id == item.id }.sumOf { it.amount }
            count / required
        }
        if (min > max) {
            max = min
        }
        if (overlap.maximum in 1..<max) {
            max = overlap.maximum
        }
    }
    return max
}

fun Transaction.removeItems(def: ItemOnItemDefinition, success: Boolean): String {
    for (item in def.requires) {
        if (!inventory.contains(item.id, item.amount)) {
            error = TransactionError.Deficient(item.amount)
            return "You need a ${item.def.name.lowercase()} to ${def.type} this."
        }
    }
    for (item in def.remove) {
        remove(item.id, item.amount)
        if (failed) {
            return "You don't have enough ${item.def.name.lowercase().plural(item.amount)} to ${def.type} this."
        }
    }
    var removedOne = def.one.isEmpty()
    for (item in def.one) {
        if (inventory.contains(item.id, item.amount)) {
            remove(item.id, item.amount)
            removedOne = true
        }
    }
    if (!removedOne || failed) {
        val first = def.one.first()
        return "You don't have enough ${first.def.name.lowercase().plural(first.amount)} to ${def.type} this."
    }

    for (add in if (success) def.add else def.fail) {
        add(add.id, add.amount)
        if (failed) {
            return "You don't have enough inventory space to ${def.type} this."
        }
    }
    return ""
}