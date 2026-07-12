package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.inv.transact.operation.SetCharge.setCharge
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * A helm that can be enchanted to hold combat Summoning scrolls - a row of the `enchanted_headgear`
 * table (see `enchanted_headgear.tables.toml`): its plain, tradeable [base] form (the row id), the
 * empty [enchanted] form Pikkupstix converts it into, the [charged] form it takes while holding
 * scrolls, the Summoning [level] to enchant it and the scroll [capacity].
 */
private val RowDefinition.base: String get() = rowId
private val RowDefinition.enchanted: String get() = item("enchanted")
private val RowDefinition.charged: String get() = item("charged")
private val RowDefinition.level: Int get() = int("level")
private val RowDefinition.capacity: Int get() = int("capacity")

/** The headgear row [itemId] belongs to - matched against its base, enchanted or charged form. */
private fun headgear(itemId: String): RowDefinition? = Tables.get("enchanted_headgear").rows().firstOrNull {
    itemId == it.base || itemId == it.enchanted || itemId == it.charged
}

/** The scroll a charged enchanted helm currently holds (item id), and how many. Only one helm at a time. */
private const val SCROLL_ID = "enchanted_headgear_scroll"
private const val SCROLL_COUNT = "enchanted_headgear_count"

/** True if [scroll] is a combat special-move scroll (only these can be stored, per the live game). */
private fun isCombatScroll(scroll: Item): Boolean {
    if (scroll.def["special_points", 0] == 0) {
        return false
    }
    val pouch = EnumDefinitions.get("summoning_scroll_ids_2").getKey(scroll.def.id)
    val familiarNpc = EnumDefinitions.get("summoning_familiar_ids").intOrNull(pouch) ?: return false
    val familiar = NPCDefinitions.getOrNull(familiarNpc)?.stringId ?: return false
    return FamiliarSpecialMoves.npcTarget.containsKey(familiar) || FamiliarSpecialMoves.playerTarget.containsKey(familiar)
}

/**
 * Pikkupstix enchants a plain helm into its empty scroll-holding form, or disenchants it back to a
 * tradeable helm. The hunter helms are already scroll-capable (base == enchanted), so he just says
 * so; a charged helm must be emptied first. Returns true if [item] was a recognised piece of headgear.
 */
fun Player.enchantHeadgear(item: Item): Boolean {
    val headgear = headgear(item.id) ?: return false
    when (item.id) {
        headgear.charged -> message("You need to remove the scrolls before I can work on that helmet.")
        headgear.enchanted -> if (headgear.enchanted == headgear.base) {
            message("That helmet is already able to hold Summoning scrolls - just use your combat scrolls on it.")
        } else {
            inventory.replace(headgear.enchanted, headgear.base)
            message("Pikkupstix removes the enchantment from your headwear.")
        }
        else -> if (!hasMax(Skill.Summoning, headgear.level, message = true)) {
            message("You need a Summoning level of ${headgear.level} to enchant that helmet.")
        } else {
            inventory.replace(headgear.base, headgear.enchanted)
            message("Pikkupstix magically enchants your headwear.")
        }
    }
    return true
}

/**
 * The scroll a worn enchanted helm can supply for the current familiar's special, or null. Called by
 * the cast gate so a special can fire from the helm's store when the inventory has no scroll left.
 */
fun Player.enchantedHeadgearScroll(): String? {
    val helm = equipped(EquipSlot.Hat).id
    if (headgear(helm)?.charged != helm || get(SCROLL_COUNT, 0) <= 0) {
        return null
    }
    return get(SCROLL_ID, "").takeIf { it.isNotEmpty() }
}

/** Spends one scroll from the worn enchanted helm, emptying it back to its enchanted form when the last goes. */
fun Player.spendEnchantedHeadgearScroll() {
    val count = get(SCROLL_COUNT, 0) - 1
    set(SCROLL_COUNT, count)
    val headgear = headgear(equipped(EquipSlot.Hat).id) ?: return
    if (count <= 0) {
        // Reset the charge to 1 so the item reverts as a clean single helm, not one carrying the
        // old charge count.
        syncHelmCharge(headgear, 1)
        equipment.replace(EquipSlot.Hat.index, headgear.charged, headgear.enchanted)
        clear(SCROLL_ID)
        clear(SCROLL_COUNT)
    } else {
        syncHelmCharge(headgear, count)
    }
}

/**
 * Mirrors the stored scroll [count] onto the charged helm's item charge, worn or in the pack, so the
 * client shows it on the item and on the follower-details cast button.
 */
private fun Player.syncHelmCharge(headgear: RowDefinition, count: Int) {
    if (equipped(EquipSlot.Hat).id == headgear.charged) {
        equipment.transaction { setCharge(EquipSlot.Hat.index, count) }
        return
    }
    val slot = inventory.indexOf(headgear.charged)
    if (slot != -1) {
        inventory.transaction { setCharge(slot, count) }
    }
}

class EnchantedHeadgear : Script {
    init {
        for (headgear in Tables.get("enchanted_headgear").rows()) {
            // Fill an enchanted helm by using combat scrolls on it - all matching scrolls in the
            // pack, up to capacity, one scroll type at a time. The helm takes its charged form.
            // Fill an empty enchanted helm or top up an already-charged one.
            itemOnItem("*_scroll", headgear.enchanted) { fromItem, toItem ->
                store(if (fromItem.id == headgear.enchanted) toItem else fromItem, headgear)
            }
            if (headgear.charged != headgear.enchanted) {
                itemOnItem("*_scroll", headgear.charged) { fromItem, toItem ->
                    store(if (fromItem.id == headgear.charged) toItem else fromItem, headgear)
                }
            }
            // Right-click the charged helm to check its contents or empty it back to the enchanted
            // form, returning the scrolls to the pack.
            itemOption("Commune", headgear.charged) { commune() }
            itemOption("Commune", headgear.charged, "worn_equipment") { commune() }
            itemOption("Uncharge", headgear.charged) { uncharge(headgear) }
            itemOption("Uncharge", headgear.charged, "worn_equipment") { uncharge(headgear) }
        }
    }

    private fun Player.store(scroll: Item, headgear: RowDefinition) {
        if (!isCombatScroll(scroll)) {
            message("Only combat scrolls can be stored in headgear.")
            return
        }
        val stored = get(SCROLL_COUNT, 0)
        if (stored > 0 && get(SCROLL_ID, "") != scroll.id) {
            message("This helmet already holds ${ItemDefinitions.get(get(SCROLL_ID, "")).name.lowercase()}s. Empty it first.")
            return
        }
        val room = headgear.capacity - stored
        if (room <= 0) {
            message("The helmet is full.")
            return
        }
        val amount = minOf(room, inventory.count(scroll.id))
        if (amount <= 0 || !inventory.remove(scroll.id, amount)) {
            return
        }
        set(SCROLL_ID, scroll.id)
        set(SCROLL_COUNT, stored + amount)
        // Turn the empty enchanted helm into its charged form; a helm that's already charged (a
        // top-up, or a worn one) just has its charge count updated.
        if (equipped(EquipSlot.Hat).id != headgear.charged && inventory.contains(headgear.enchanted)) {
            inventory.replace(headgear.enchanted, headgear.charged)
        }
        syncHelmCharge(headgear, stored + amount)
        message("You store $amount ${scroll.def.name.lowercase()}${if (amount == 1) "" else "s"} in the helmet (${stored + amount}/${headgear.capacity}).", ChatType.Filter)
    }

    private fun Player.commune() {
        val count = get(SCROLL_COUNT, 0)
        val scroll = get(SCROLL_ID, "")
        if (count <= 0 || scroll.isEmpty()) {
            message("The helmet holds no scrolls.")
            return
        }
        message("The helmet holds $count ${ItemDefinitions.get(scroll).name.lowercase()}${if (count == 1) "" else "s"}.")
    }

    private fun Player.uncharge(headgear: RowDefinition) {
        val scroll = get(SCROLL_ID, "")
        val count = get(SCROLL_COUNT, 0)
        if (scroll.isNotEmpty() && count > 0) {
            inventory.add(scroll, count)
            message("You empty the helmet, recovering $count ${ItemDefinitions.get(scroll).name.lowercase()}${if (count == 1) "" else "s"}.", ChatType.Filter)
        }
        // Reset the charge to 1 so the helm reverts as a clean single item, then revert it to its
        // empty enchanted form whether or not it held anything.
        syncHelmCharge(headgear, 1)
        if (equipped(EquipSlot.Hat).id == headgear.charged) {
            equipment.replace(EquipSlot.Hat.index, headgear.charged, headgear.enchanted)
        } else {
            inventory.replace(headgear.charged, headgear.enchanted)
        }
        clear(SCROLL_ID)
        clear(SCROLL_COUNT)
    }
}
