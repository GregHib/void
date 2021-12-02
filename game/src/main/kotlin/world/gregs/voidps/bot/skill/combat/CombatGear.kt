package world.gregs.voidps.bot.skill.combat

import world.gregs.voidps.bot.bank.*
import world.gregs.voidps.bot.buyItem
import world.gregs.voidps.bot.equip
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.weightedSample
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.interact.entity.combat.ammo
import world.gregs.voidps.world.interact.entity.combat.spellBook
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes
import world.gregs.voidps.world.interact.entity.player.combat.magic.magicLevel
import world.gregs.voidps.world.interact.entity.player.combat.magic.spellRequiredItems
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo.isBowOrCrossbow
import world.gregs.voidps.world.interact.entity.player.equip.EquipBonuses
import world.gregs.voidps.world.interact.entity.player.equip.hasRequirements
import world.gregs.voidps.world.interact.entity.player.equip.slot

suspend fun Bot.setupCombatGear(skill: Skill, races: Set<String>) {
    openBank()
    depositAll()
    depositWornItems()
    setupGear(skill, races)
}

private val slots = listOf(
    EquipSlot.Weapon,
    EquipSlot.Ammo,
    EquipSlot.Shield,
    EquipSlot.Amulet,
    EquipSlot.Chest,
    EquipSlot.Legs,
    EquipSlot.Feet,
    EquipSlot.Hat,
    EquipSlot.Cape,
    EquipSlot.Hands,
    EquipSlot.Ring
)

private suspend fun Bot.setupGear(skill: Skill, targetRaces: Set<String>) {
    val map = mutableMapOf<EquipSlot, Item>()
    for (item in player.bank.getItems()) {
        if (item.isEmpty() || item.slot == EquipSlot.None) {
            continue
        }
        if (item.slot == EquipSlot.Weapon && skill != Skill.Range && isBowOrCrossbow(item)) {
            continue
        }
        if (player.hasRequirements(item) && isBetter(item, map.getOrDefault(item.slot, Item.EMPTY), skill)) {
            map[item.slot] = item
        }
    }

    val toBuy = mutableListOf<EquipSlot>()
    for (slot in slots) {
        if (slot == EquipSlot.Ammo && skill != Skill.Range) {
            continue
        }
        val item = map[slot]
        if (item == null || item.isEmpty()) {
            toBuy.add(slot)
        } else {
            if (slot == EquipSlot.Ammo) {
                withdrawAll(item.id)
            } else {
                withdraw(item.id, amount = 1)
            }
            equip(item.id)
        }
    }

    if (skill == Skill.Magic) {
        withdrawSpellRequirements()
        if (player.equipped(EquipSlot.Weapon).isNotEmpty()) {
            toBuy.remove(EquipSlot.Weapon)
        }
    }

    if (toBuy.isNotEmpty()) {
        goShopping(toBuy, skill)
    } else {
        closeBank()
    }

    if (skill == Skill.Range) {
        if (player.equipped(EquipSlot.Ammo).isEmpty() || !player.equipped(EquipSlot.Weapon).def.ammo.contains(player.equipped(EquipSlot.Ammo).id)) {
            throw NullPointerException("No ammo found for range gear setup.")
        }
    }
}

private suspend fun Bot.withdrawSpellRequirements() {
    val components = get<InterfaceDefinitions>().get(player.spellBook).components!!.values
    val component = components
        .filter { isUsableOffensiveSpell(player, it) && Runes.getCastCount(player, it) >= 50 }
        .maxByOrNull { it.magicLevel }
        ?: components
            .filter { isUsableOffensiveSpell(player, it) }
            .maxBy { it.magicLevel }!!

    for (item in component.spellRequiredItems()) {
        if (player.bank.contains(item.id)) {
            withdrawAll(item.id)
        } else {
            buyItem(item.id, 100)
        }
        if (item.id.endsWith("_staff")) {
            equip(item.id)
        }
    }
    setAutoCast(component.stringId)
}

private fun isUsableOffensiveSpell(player: Player, definition: InterfaceComponentDefinition): Boolean {
    if (!player.has(Skill.Magic, definition.magicLevel, message = false)) {
        return false
    }
    if (!definition.extras.contains("cast_id")) {
        return false
    }
    return true
}

private suspend fun Bot.goShopping(toBuy: List<EquipSlot>, skill: Skill) {
    val areas: Areas = get()
    val itemDefinitions: ItemDefinitions = get()
    val shopItems = areas.getTagged("shop")
        .flatMap { it.tags }
        .filter { it != "shop" }
        .map { itemDefinitions.get(it) }
        .filter { toBuy.contains(it.slot) }
        .groupBy { it.slot }
    for (slot in toBuy) {
        val items = shopItems[slot] ?: continue
        val coins = player.bank.getCount("coins") + player.inventory.getCount("coins")
        val scored = items
            .asSequence()
            .filter { player.hasRequirements(it) && it.cost < coins }
            .map { it.stringId to score(it, skill) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(items.size / 10)
            .toList()
        val item = if (scored.size == 1) scored.first().first else weightedSample(scored)
        if (item != null) {
            buyItem(item, amount = if (slot == EquipSlot.Ammo) 100 else 1)
            equip(item)
        }
    }
}


private fun isBetter(item: Item, current: Item, skill: Skill): Boolean {
    if (item.isEmpty()) {
        return false
    }
    if (current.isEmpty()) {
        return true
    }
    return score(item.def, skill) > score(current.def, skill)
}

private fun score(definition: ItemDefinition, skill: Skill): Double {
    return when (skill) {
        Skill.Range -> definition.extras
            .filterKeys { it.startsWith("range") && EquipBonuses.nameMap.values.contains(it) }
        Skill.Magic -> definition.extras
            .filterKeys { it.startsWith("magic") && EquipBonuses.nameMap.values.contains(it) }
        else -> definition.extras
            .filterKeys { EquipBonuses.nameMap.values.contains(it) }
    }.values.sumOf { it as Double }
}