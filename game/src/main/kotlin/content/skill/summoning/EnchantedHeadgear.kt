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
 * the empty [enchanted] form Pikkupstix converts it into, the [charged] form it takes while holding
 * scrolls, the Summoning [level] to enchant it and the scroll [capacity]. For the hunter helms
 * (antlers/lizard skull/feather headdress) the plain form is already the enchanted one - so
 * [base] == [enchanted] and there's no separate Pikkupstix step, matching darkan.
 */
private data class Headgear(val base: String, val enchanted: String, val charged: String, val level: Int, val capacity: Int)

private val HEADGEAR = listOf(
    // Hunter helms - plain form is already scroll-capable.
    Headgear("antlers", "antlers", "antlers_charged", 10, 40),
    Headgear("lizard_skull", "lizard_skull", "lizard_skull_charged", 30, 65),
    Headgear("feather_headdress_blue", "feather_headdress_blue", "feather_headdress_charged_blue", 50, 150),
    Headgear("feather_headdress_yellow", "feather_headdress_yellow", "feather_headdress_charged_yellow", 50, 150),
    Headgear("feather_headdress_red", "feather_headdress_red", "feather_headdress_charged_red", 50, 150),
    Headgear("feather_headdress_stripy", "feather_headdress_stripy", "feather_headdress_charged_stripy", 50, 150),
    Headgear("feather_headdress_orange", "feather_headdress_orange", "feather_headdress_charged_orange", 50, 150),
    // Metal/god helms - Pikkupstix enchants the plain helm first (base -> enchanted).
    Headgear("snakeskin_bandana", "snakeskin_bandana_enchanted", "snakeskin_bandana_charged", 20, 50),
    Headgear("archer_helm", "archer_helm_enchanted", "archer_helm_charged", 30, 70),
    Headgear("berserker_helm", "berserker_helm_enchanted", "berserker_helm_charged", 30, 70),
    Headgear("warrior_helm", "warrior_helm_enchanted", "warrior_helm_charged", 30, 70),
    Headgear("farseer_helm", "farseer_helm_enchanted", "farseer_helm_charged", 30, 70),
    Headgear("rune_full_helm", "rune_full_helm_enchanted", "rune_full_helm_charged", 30, 60),
    Headgear("splitbark_helm", "splitbark_helm_enchanted", "splitbark_helm_charged", 30, 50),
    Headgear("helm_of_neitiznot", "helm_of_neitiznot_enchanted", "helm_of_neitiznot_charged", 45, 90),
    Headgear("dragon_med_helm", "dragon_helm_enchanted", "dragon_helm_charged", 50, 110),
    Headgear("lunar_helm", "lunar_helm_enchanted", "lunar_helm_charged", 55, 110),
    Headgear("armadyl_helmet", "armadyl_helmet_enchanted", "armadyl_helmet_charged", 60, 120),
)

private val BY_ID = HEADGEAR.flatMap { listOf(it.base to it, it.enchanted to it, it.charged to it) }.toMap()

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
    val headgear = BY_ID[item.id] ?: return false
    when (item.id) {
        headgear.charged -> message("You need to remove the scrolls before I can work on that helmet.")
        headgear.enchanted -> if (headgear.enchanted == headgear.base) {
            message("That helmet is already able to hold Summoning scrolls - just use your combat scrolls on it.")
        } else {
            inventory.replace(headgear.enchanted, headgear.base)
            message("Pikkupstix removes the enchantment from your headwear.")
        }
        else -> if (!has(Skill.Summoning, headgear.level, message = true)) {
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
    if (BY_ID[helm]?.charged != helm || get(SCROLL_COUNT, 0) <= 0) {
        return null
    }
    return get(SCROLL_ID, "").takeIf { it.isNotEmpty() }
}

/** Spends one scroll from the worn enchanted helm, emptying it back to its enchanted form when the last goes. */
fun Player.spendEnchantedHeadgearScroll() {
    val count = get(SCROLL_COUNT, 0) - 1
    set(SCROLL_COUNT, count)
    if (count <= 0) {
        val helm = equipped(EquipSlot.Hat).id
        BY_ID[helm]?.let { equipment.replace(EquipSlot.Hat.index, it.charged, it.enchanted) }
        clear(SCROLL_ID)
        clear(SCROLL_COUNT)
    }
}

class EnchantedHeadgear : Script {
    init {
        for (headgear in HEADGEAR) {
            // Fill an enchanted helm by using combat scrolls on it - all matching scrolls in the
            // pack, up to capacity, one scroll type at a time. The helm takes its charged form.
            itemOnItem("*_scroll", headgear.enchanted) { fromItem, toItem ->
                store(if (fromItem.id == headgear.enchanted) toItem else fromItem, headgear)
            }
            // Right-click the charged helm to check its contents or empty it back to the enchanted
            // form, returning the scrolls to the pack.
            itemOption("Commune", headgear.charged) { commune() }
            itemOption("Commune", headgear.charged, "worn_equipment") { commune() }
            itemOption("Uncharge", headgear.charged) { uncharge(headgear) }
            itemOption("Uncharge", headgear.charged, "worn_equipment") { uncharge(headgear) }
        }
    }

    private fun Player.store(scroll: Item, headgear: Headgear) {
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
        inventory.replace(headgear.enchanted, headgear.charged)
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

    private fun Player.uncharge(headgear: Headgear) {
        val scroll = get(SCROLL_ID, "")
        val count = get(SCROLL_COUNT, 0)
        if (scroll.isNotEmpty() && count > 0) {
            inventory.add(scroll, count)
            message("You empty the helmet, recovering $count ${ItemDefinitions.get(scroll).name.lowercase()}${if (count == 1) "" else "s"}.", ChatType.Filter)
        }
        // The charged helm reverts to its empty enchanted form whether or not it held anything.
        if (equipped(EquipSlot.Hat).id == headgear.charged) {
            equipment.replace(EquipSlot.Hat.index, headgear.charged, headgear.enchanted)
        } else {
            inventory.replace(headgear.charged, headgear.enchanted)
        }
        clear(SCROLL_ID)
        clear(SCROLL_COUNT)
    }
}
