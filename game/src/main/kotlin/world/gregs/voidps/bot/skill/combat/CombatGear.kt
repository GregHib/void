package world.gregs.voidps.bot.skill.combat

import world.gregs.voidps.bot.bank.*
import world.gregs.voidps.bot.buyItem
import world.gregs.voidps.bot.equip
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.GearDefinitions
import world.gregs.voidps.engine.entity.definition.config.GearDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.bank.has
import world.gregs.voidps.world.interact.entity.player.equip.hasRequirements
import world.gregs.voidps.world.interact.entity.player.equip.hasUseRequirements

suspend fun Bot.setupGear(skill: Skill) {
    openBank()
    depositAll()
    depositWornItems()
    setupGearAndInv(skill)
}

private fun Bot.getGear(skill: Skill): GearDefinition? {
    val style = when (skill) {
        Skill.Attack, Skill.Strength, Skill.Defence -> "melee"
        else -> skill.name.toLowerCase()
    }

    val setups = get<GearDefinitions>().get(style)
    val level = player.levels.getMax(skill)
    return setups
        .filter { it.levels.contains(level) }
        .maxByOrNull { player.gearScore(it) }
}

private fun Player.gearScore(definition: GearDefinition): Double {
    val total = definition.inventory.size + definition.equipment.size
    if (total <= 0) {
        return 0.0
    }
    var count = 0
    for (item in definition.inventory) {
        if (hasRequirements(item) && has(item.id, item.amount, banked = true)) {
            count++
        }
    }
    for ((_, equipment) in definition.equipment) {
        for (item in equipment) {
            if (hasRequirements(item) && has(item.id, item.amount, banked = true)) {
                count++
            }
        }
    }
    return count / total.toDouble()
}

fun Bot.hasExactGear(skill: Skill): Boolean {
    val gear = getGear(skill)
    if (gear != null) {
        val score = player.gearScore(gear)
        return score == 1.0
    }
    return false
}

private suspend fun Bot.setupGearAndInv(skill: Skill) {
    val gear = getGear(skill)!!
    for ((_, equipmentList) in gear.equipment) {
        val items = equipmentList
            .filter { player.hasRequirements(it) || player.hasUseRequirements(it) || player.bank.contains(it.id, it.amount) }
        if (items.isEmpty()) {
            continue
        }
        for (item in items) {
            if (withdrawOrBuy(item)) {
                break
            }
        }
    }

    for (item in gear.inventory) {
        if (withdrawOrBuy(item)) {
            continue
        }
    }

    if (player.inventory.contains("coins")) {
        openBank()
        depositAll("coins")
    }
    if (skill == Skill.Magic) {
        setAutoCast(gear["spell"])
    }
    closeBank()
}

suspend fun Bot.withdrawOrBuy(item: Item): Boolean {
    if (player.bank.contains(item.id, item.amount)) {
        if (item.amount == 1) {
            withdraw(item.id)
        } else {
            withdrawAll(item.id)
        }
        return true
    }
    if (buyItem(item.id, item.amount)) {
        equip(item.id)
        return true
    }
    return false
}