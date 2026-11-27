package content.bot.behaviour.loadout

import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * A named list of interchangeable items for a single equipment slot, ordered best to worst.
 * E.g. melee_weapons = [rune_scimitar, ..., bronze_sword]
 * Defined in `*.gear.toml` files and referenced by [Loadout] slots.
 */
class Gear(val id: String, val ids: List<String>) {
    /**
     * Item requirements are cached on first use as item definitions may not be loaded when the gear is.
     */
    val items: List<GearItem> by lazy { ids.map { GearItem.of(it) ?: error("Unknown item '$it' in gear '$id'.") } }

    val slot: EquipSlot
        get() = items.firstOrNull()?.slot ?: EquipSlot.None

    override fun toString() = "Gear($id)"
}
