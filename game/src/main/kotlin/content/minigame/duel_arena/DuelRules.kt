package content.minigame.duel_arena

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * Rule names match the interface component names and the "duel_" prefixed varbits.
 */
object DuelRules {

    val styles = listOf("no_ranged", "no_melee", "no_magic")

    val general = listOf(
        "no_ranged",
        "no_melee",
        "no_magic",
        "fun_weapons",
        "no_forfeit",
        "no_drinks",
        "no_food",
        "no_prayer",
        "no_movement",
        "obstacles",
        "summoning",
        "no_special",
    )

    val equipment: Map<String, EquipSlot> = mapOf(
        "no_hat" to EquipSlot.Hat,
        "no_cape" to EquipSlot.Cape,
        "no_amulet" to EquipSlot.Amulet,
        "no_ammo" to EquipSlot.Ammo,
        "no_weapon" to EquipSlot.Weapon,
        "no_body" to EquipSlot.Chest,
        "no_shield" to EquipSlot.Shield,
        "no_legs" to EquipSlot.Legs,
        "no_ring" to EquipSlot.Ring,
        "no_boots" to EquipSlot.Feet,
        "no_gloves" to EquipSlot.Hands,
    )

    val all: List<String> = general + equipment.keys

    fun info(rule: String): String = when (rule) {
        "no_ranged" -> "You cannot use Ranged attacks."
        "no_melee" -> "You cannot use Melee attacks."
        "no_magic" -> "You cannot use Magic attacks."
        "fun_weapons" -> "You can only attack with 'fun' weapons."
        "no_forfeit" -> "You cannot forfeit the duel."
        "no_drinks" -> "You cannot use potions."
        "no_food" -> "You cannot use food."
        "no_prayer" -> "You cannot use Prayer."
        "no_movement" -> "You cannot move."
        "obstacles" -> "There will be obstacles in the arena."
        "summoning" -> "Familiars will be allowed in the arena."
        "no_special" -> "You cannot use special attacks."
        else -> ""
    }

    fun hasEquipmentRule(duel: Duel): Boolean = equipment.keys.any { duel.hasRule(it) }

    fun funWeapon(item: Item): Boolean = item.def["fun_weapon", false]

    fun hasFunWeapon(player: Player): Boolean = player.inventory.items.any { funWeapon(it) } || player.equipment.items.any { funWeapon(it) }

    /**
     * Worn items that will be removed when the duel starts
     */
    fun removedEquipment(player: Player, duel: Duel): List<EquipSlot> {
        val slots = mutableListOf<EquipSlot>()
        for ((rule, slot) in equipment) {
            if (duel.hasRule(rule) && player.equipped(slot).isNotEmpty()) {
                slots.add(slot)
            }
        }
        if (duel.hasRule("no_shield") && !duel.hasRule("no_weapon") && player.equipped(EquipSlot.Weapon).type == EquipType.TwoHanded) {
            slots.add(EquipSlot.Weapon)
        }
        return slots
    }

    fun blockedSlots(duel: Duel): Set<EquipSlot> = equipment.filterKeys { duel.hasRule(it) }.values.toSet()

    fun restricted(blocked: Set<EquipSlot>, item: Item): Boolean = blocked.contains(item.slot) || (blocked.contains(EquipSlot.Shield) && item.type == EquipType.TwoHanded)
}
