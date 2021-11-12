package world.gregs.voidps.bot.skill.combat

import world.gregs.voidps.bot.bank.closeBank
import world.gregs.voidps.bot.bank.depositAll
import world.gregs.voidps.bot.bank.openBank
import world.gregs.voidps.bot.bank.withdraw
import world.gregs.voidps.bot.buyItem
import world.gregs.voidps.bot.equip
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.items
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.interact.entity.combat.getWeaponType
import world.gregs.voidps.world.interact.entity.player.equip.hasRequirements
import world.gregs.voidps.world.interact.entity.player.equip.requiredLevel
import world.gregs.voidps.world.interact.entity.player.equip.slot
import kotlin.math.abs

suspend fun Bot.setupCombatGear(skill: Skill, races: Set<String>) {
    when (skill) {
        Skill.Attack, Skill.Strength, Skill.Defence -> setupMeleeGear(skill, races)
        Skill.Range -> setupRangeGear(races)
        Skill.Magic -> setupMagicGear(races)
        else -> return
    }
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

private suspend fun Bot.setupMeleeGear(skill: Skill, targetRaces: Set<String>) {
    val weapon = player.equipped(EquipSlot.Weapon)
    var banked = false
    for (slot in slots) {
        val bestOwned = getBestOwnedEquipment(slot)
        if (bestOwned == null || abs(bestOwned.maxRequirement - player.levels.getMax(skill)) > 10) {
            val bestShop = getBestUsableShopEquipment(slot, when (slot) {
                EquipSlot.Weapon -> "zekes_superior_scimitars"
                else -> continue
            })
            if (bestShop != null && bestOwned.maxRequirement < bestShop.maxRequirement) {
                buyItem(bestShop.id)
                equip(bestShop.id)
                continue
            }
        }

        val current = player.equipped(slot)
        if (current.isNotEmpty() && getWeaponType(player, weapon) == "melee") {
            continue
        }

        val bestEquipment = player.bank.getItems()
            .filter { it.slot == slot && player.hasRequirements(it) }
            .maxByOrNull { it.maxRequirement } ?: continue
        openBank()
        if (!banked) {
            depositAll()
            banked = true
        }
        withdraw(bestEquipment.id)
        equip(bestEquipment.id)
        closeBank()
    }
}

private fun Bot.getBestUsableShopEquipment(slot: EquipSlot, shop: String): Item? {
    val container: ContainerDefinition = get<ContainerDefinitions>().get(shop)
    return container.items()
        .map { Item(it) }
        .filter { it.slot == slot && player.hasRequirements(it) }
        .maxByOrNull { it.maxRequirement }
}

private val Item?.maxRequirement: Int
    get() = if (this == null) 1 else (0..10).maxOf { def.requiredLevel(it) }

private fun Bot.getBestOwnedEquipment(slot: EquipSlot): Item? {
    val current = player.equipped(slot)
    if (player.hasRequirements(current.def)) {
        return current
    }
    val inventoryItem = player.inventory.getItems()
        .filter { it.slot == slot && player.hasRequirements(it.def) }
        .maxByOrNull { it.maxRequirement }
    if (inventoryItem != null) {
        return inventoryItem
    }
    return player.bank.getItems()
        .filter { it.slot == slot && player.hasRequirements(it) }
        .maxByOrNull { it.maxRequirement }
}


private suspend fun Bot.setupRangeGear(races: Set<String>) {

}


private suspend fun Bot.setupMagicGear(races: Set<String>) {

}
