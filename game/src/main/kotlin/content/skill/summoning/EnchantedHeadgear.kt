package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * A helm that can be enchanted to hold combat Summoning scrolls: its plain, tradeable form ([base]),
 * the charged form it takes while holding scrolls ([charged]), the [level] to enchant it and the
 * [capacity] of scrolls it holds. For void's hunter helms the plain form is already the enchanted
 * one (matching darkan's base==enchanted for these), so charging is the only visible state change.
 */
private data class Headgear(val base: String, val charged: String, val level: Int, val capacity: Int)

private val HEADGEAR = listOf(
    Headgear("antlers", "antlers_charged", 10, 40),
    Headgear("lizard_skull", "lizard_skull_charged", 30, 65),
    Headgear("feather_headdress_blue", "feather_headdress_charged_blue", 50, 150),
    Headgear("feather_headdress_yellow", "feather_headdress_charged_yellow", 50, 150),
    Headgear("feather_headdress_red", "feather_headdress_charged_red", 50, 150),
    Headgear("feather_headdress_stripy", "feather_headdress_charged_stripy", 50, 150),
    Headgear("feather_headdress_orange", "feather_headdress_charged_orange", 50, 150),
).flatMap { listOf(it.base to it, it.charged to it) }.toMap()

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
 * Pikkupstix enchants (or unenchants) a piece of headwear. For the hunter helms void carries the
 * plain form already holds scrolls, so there's nothing to convert - he says as much. Charged helms
 * must be emptied first. Returns true if [item] was a recognised piece of headgear.
 */
fun Player.enchantHeadgear(item: Item): Boolean {
    val headgear = HEADGEAR[item.id] ?: return false
    when (item.id) {
        headgear.charged -> message("You need to remove the scrolls before I can work on that helmet.")
        else -> message("That helmet is already able to hold Summoning scrolls - just use your combat scrolls on it to fill it.")
    }
    return true
}

/**
 * The scroll a worn enchanted helm can supply for the current familiar's special, or null. Called by
 * the cast gate so a special can fire from the helm's store when the inventory has no scroll left.
 */
fun Player.enchantedHeadgearScroll(): String? {
    val helm = equipped(EquipSlot.Hat).id
    if (HEADGEAR[helm]?.charged != helm || get(SCROLL_COUNT, 0) <= 0) {
        return null
    }
    return get(SCROLL_ID, "").takeIf { it.isNotEmpty() }
}

/** Spends one scroll from the worn enchanted helm, emptying it back to its plain form when the last goes. */
fun Player.spendEnchantedHeadgearScroll() {
    val count = get(SCROLL_COUNT, 0) - 1
    set(SCROLL_COUNT, count)
    if (count <= 0) {
        val helm = equipped(EquipSlot.Hat).id
        HEADGEAR[helm]?.let { equipment.replace(EquipSlot.Hat.index, it.charged, it.base) }
        clear(SCROLL_ID)
        clear(SCROLL_COUNT)
    }
}

class EnchantedHeadgear : Script {
    init {
        // Fill a helm by using combat scrolls on it - all matching scrolls in the pack, up to the
        // helm's capacity. A helm holds one scroll type at a time; the plain helm becomes its
        // charged form.
        for (headgear in HEADGEAR.values.distinct()) {
            itemOnItem("*_scroll", headgear.base) { fromItem, toItem ->
                store(if (fromItem.id == headgear.base) toItem else fromItem, headgear)
            }
            // Empty a charged helm back to its plain form on a summoning obelisk; scrolls return.
            itemOnObjectOperate(headgear.charged, "obelisk") {
                withdraw(headgear)
            }
        }
    }

    private fun Player.store(scroll: Item, headgear: Headgear) {
        if (!isCombatScroll(scroll)) {
            message("Only combat scrolls can be stored in headgear.")
            return
        }
        if (!has(Skill.Summoning, headgear.level, message = true)) {
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
        inventory.replace(headgear.base, headgear.charged)
        message("You store $amount ${scroll.def.name.lowercase()}${if (amount == 1) "" else "s"} in the helmet (${stored + amount}/${headgear.capacity}).", ChatType.Filter)
    }

    private fun Player.withdraw(headgear: Headgear) {
        val scroll = get(SCROLL_ID, "")
        val count = get(SCROLL_COUNT, 0)
        if (scroll.isEmpty() || count <= 0) {
            inventory.replace(headgear.charged, headgear.base)
            clear(SCROLL_ID)
            clear(SCROLL_COUNT)
            return
        }
        inventory.add(scroll, count)
        inventory.replace(headgear.charged, headgear.base)
        clear(SCROLL_ID)
        clear(SCROLL_COUNT)
        message("You empty the helmet, recovering $count ${ItemDefinitions.get(scroll).name.lowercase()}${if (count == 1) "" else "s"}.", ChatType.Filter)
    }
}
