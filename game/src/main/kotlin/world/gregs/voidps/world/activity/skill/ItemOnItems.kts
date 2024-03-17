package world.gregs.voidps.world.activity.skill

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.data.definition.ItemOnItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.entity.combat.underAttack
import world.gregs.voidps.world.interact.entity.sound.playSound

val itemOnItemDefs: ItemOnItemDefinitions by inject()

itemOnItem { player ->
    val overlaps = itemOnItemDefs.getOrNull(fromItem, toItem) ?: return@itemOnItem
    if (overlaps.isEmpty()) {
        return@itemOnItem
    }
    player.closeInterfaces()
    player.weakQueue("item_on_item") {
        val maximum = getMaximum(overlaps, player)
        val (def, amount) = if (makeImmediately(player, overlaps, maximum)) {
            player.closeDialogue()
            overlaps.first() to 1
        } else {
            val type = overlaps.first().type
            val (selection, amount) = makeAmount(
                overlaps.map { it.add.first().id }.distinct().toList(),
                type = type.toSentenceCase(),
                maximum = maximum,
                text = "How many would you like to $type?"
            )
            overlaps.first { it.add.first().id == selection } to amount
        }
        val skill = def.skill
        if (amount <= 0) {
            hasItems(player, def)
            return@weakQueue
        }
        useItemOnItem(player, skill, def, amount, 0)
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
        return
    }

    if (skill != null && !player.has(skill, def.level, true)) {
        return
    }

    if (player.inventory.spaces + def.remove.size - (if (def.one.isEmpty()) 0 else 1) - def.add.size < 0) {
        player.inventoryFull()
        return
    }

    if (!hasItems(player, def)) {
        return
    }
    player.weakQueue("item_on_item_start", 1) {
        if (def.animation.isNotEmpty()) {
            player.setAnimation(def.animation)
        }
        if (def.graphic.isNotEmpty()) {
            player.setGraphic(def.graphic)
        }
        if (def.sound.isNotEmpty()) {
            player.playSound(def.sound)
        }
        if (count == 0 && def.delay > 0) {
            player.weakQueue("item_on_item_first", def.delay) {
                replaceItems(def, player, skill, amount, count)
            }
        } else if (count != 0 || def.delay != -1) {
            player.weakQueue("item_on_item_delay", def.ticks) {
                replaceItems(def, player, skill, amount, count)
            }
        }
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
    if (def.remove.any { !player.inventory.contains(it.id, it.amount) }) {
        return
    }
    if (def.one.isNotEmpty() && def.one.none { player.inventory.contains(it.id, it.amount) }) {
        return
    }
    for (remove in def.remove) {
        player.inventory.remove(remove.id, remove.amount)
    }
    for (remove in def.one) {
        if (player.inventory.remove(remove.id, remove.amount)) {
            break
        }
    }
    val success = Level.success(if (skill == null) 1 else player.levels.get(skill), def.chance)
    if (skill == null || success) {
        if (def.message.isNotEmpty()) {
            player.message(def.message, ChatType.Filter)
        }
        if (skill != null) {
            player.exp(skill, def.xp)
        }
        for (add in def.add) {
            player.inventory.add(add.id, add.amount)
        }
        player.emit(ItemUsedOnItem(def))
    } else {
        if (def.failure.isNotEmpty()) {
            player.message(def.failure, ChatType.Filter)
        }
        for (add in def.fail) {
            player.inventory.add(add.id, add.amount)
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

fun makeImmediately(player: Player, overlaps: List<ItemOnItemDefinition>, maximum: Int): Boolean {
    return (overlaps.size == 1 && maximum == 1) || player["selecting_amount", false] || player.underAttack
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
    }
    return max
}

fun hasItems(player: Player, def: ItemOnItemDefinition): Boolean {
    for (item in def.requires) {
        if (!player.inventory.contains(item.id, item.amount)) {
            player.message("You need a ${item.def.name.lowercase()} to ${def.type} this.")
            return false
        }
    }
    for (item in def.remove) {
        if (!player.inventory.contains(item.id, item.amount)) {
            player.message("You don't have enough ${item.def.name.lowercase()} to ${def.type} this.")
            return false
        }
    }
    if (def.one.isNotEmpty() && def.one.none { item -> player.inventory.contains(item.id, item.amount) }) {
        player.message("You don't have enough ${def.one.first().def.name.lowercase()} to ${def.type} this.")
        return false
    }
    return true
}
