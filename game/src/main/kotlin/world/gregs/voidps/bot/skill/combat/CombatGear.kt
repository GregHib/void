package world.gregs.voidps.bot.skill.combat

import world.gregs.voidps.bot.bank.*
import world.gregs.voidps.bot.buyItem
import world.gregs.voidps.bot.equip
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

suspend fun Bot.setupCombatGear(skill: Skill, races: Set<String>) {
    openBank()
    depositAll()
    depositWornItems()
    setupGear(skill, races)
}

private fun Bot.getGear(skill: Skill): GearDefinition? {
    val style = when (skill) {
        Skill.Magic -> "magic"
        Skill.Range -> "range"
        else -> "melee"
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

private suspend fun Bot.setupGear(skill: Skill, targetRaces: Set<String>) {
    val gear = getGear(skill)
    assert(gear != null) { "No suitable ${skill.name.toLowerCase()} gear found." }
    val toBuy = mutableSetOf<Item>()
    for ((_, equipmentList) in gear!!.equipment) {
        val item = equipmentList.firstOrNull { player.hasRequirements(it) && player.bank.contains(it.id, it.amount) }
        if (item != null) {
            if (item.amount == 1) {
                withdraw(item.id)
            } else {
                withdrawAll(item.id)
            }
            equip(item.id)
        } else {
            toBuy.add(equipmentList.first())
        }
    }

    for (item in gear.inventory) {
        if (player.bank.contains(item.id, item.amount)) {
            if (item.amount == 1) {
                withdraw(item.id)
            } else {
                withdrawAll(item.id)
            }
        } else {
            toBuy.add(item)
        }
    }

    if (toBuy.isNotEmpty()) {
        for (item in toBuy) {
            buyItem(item.id, item.amount)
            equip(item.id)
        }
        openBank()
        depositAll("coins")
        closeBank()
    } else {
        closeBank()
    }
    if (skill == Skill.Magic) {
        setAutoCast(gear["spell"])
    }
}