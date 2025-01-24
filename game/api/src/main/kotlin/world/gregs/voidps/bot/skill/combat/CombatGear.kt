package world.gregs.voidps.bot.skill.combat

import kotlinx.coroutines.CancellationException
import world.gregs.voidps.bot.Bot
import world.gregs.voidps.bot.bank.*
import world.gregs.voidps.bot.buyItem
import world.gregs.voidps.bot.equip
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.data.definition.GearDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.bank.ownsItem

suspend fun Bot.setupGear(gear: GearDefinition, buy: Boolean = true) {
    openBank()
    depositAll()
    if (gear.equipment.isNotEmpty()) {
        depositWornItems()
    }
    setupGearAndInv(gear, buy)
}

suspend fun Bot.setupGear(skill: Skill, buy: Boolean = true) {
    val gear = getGear(skill) ?: return
    setupGear(gear, buy)
}

fun Bot.getGear(skill: Skill): GearDefinition? {
    val style = when (skill) {
        Skill.Attack, Skill.Strength, Skill.Defence -> "melee"
        else -> skill.name.lowercase()
    }
    return getGear(style, skill)
}

fun Bot.getGear(type: String, skill: Skill): GearDefinition? {
    val setups = get<GearDefinitions>().get(type)
    val level = player.levels.getMax(skill)
    return setups
        .filter { it.levels.contains(level) }
        .sortedWith(compareBy({ player.gearScore(it) }, { it.inventory.size + it.equipment.size }))
        .lastOrNull()
}

fun Bot.getSuitableItem(items: List<Item>): Item {
    return items.first { item -> player.hasRequirements(item) && player.ownsItem(item.id, item.amount) }
}

private fun Player.gearScore(definition: GearDefinition): Double {
    val total = definition.inventory.size + definition.equipment.size
    if (total <= 0) {
        return 0.0
    }
    var count = 0
    for (items in definition.inventory) {
        if (items.any { item -> hasRequirements(item) && ownsItem(item.id, item.amount) }) {
            count++
        }
    }
    for ((_, equipment) in definition.equipment) {
        if (equipment.any { item -> hasRequirements(item) && ownsItem(item.id, item.amount) }) {
            count++
        }
    }
    return count / total.toDouble()
}

fun Bot.hasExactGear(skill: Skill): Boolean {
    val gear = getGear(skill)
    if (gear != null) {
        return hasExactGear(gear)
    }
    return false
}

fun Bot.hasExactGear(type: String, skill: Skill): Boolean {
    val gear = getGear(type, skill)
    if (gear != null) {
        return hasExactGear(gear)
    }
    return false
}

fun Bot.hasExactGear(gear: GearDefinition): Boolean {
    return player.gearScore(gear) == 1.0
}

private suspend fun Bot.setupGearAndInv(gear: GearDefinition, buy: Boolean) {
    // Pick one of each item to equip for each required slot
    for ((_, equipmentList) in gear.equipment) {
        val items = equipmentList
            .filter { player.hasRequirements(it) || player.hasRequirementsToUse(it) || player.bank.contains(it.id, it.amount) }
        if (items.isEmpty()) {
            continue
        }
        withdrawOrBuy(items, buy)
    }
    await("tick")
    if (!player.interfaces.contains("bank")) {
        openBank()
        await("tick")
    }
    for (items in gear.inventory) {
        withdrawOrBuy(items, buy)
    }

    if (player.inventory.contains("coins")) {
        openBank()
        depositAll("coins")
    }
    if (gear.type == "magic") {
        setAutoCast(gear["spell"])
    }
    closeBank()

    await("tick")
    await("tick")
    if (!hasExactGear(gear)) {
        throw CancellationException("Doesn't have all the gear required.")
    }
}

suspend fun Bot.withdrawOrBuy(items: List<Item>, buy: Boolean): Boolean {
    for (item in items) {
        if (player.bank.contains(item.id, item.amount)) {
            withdraw(item.id, amount = item.amount)
            equip(item.id)
            return true
        }
    }
    if (buy) {
        for (item in items) {
            if (buyItem(item.id, item.amount)) {
                equip(item.id)
                return true
            }
        }
    }
    return false
}