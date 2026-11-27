package content.bot.behaviour.loadout

import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.Condition
import content.entity.player.bank.bank
import content.entity.player.bank.ownsItem
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * A set of equipment (and the inventory items it depends on) a bot uses for an activity.
 * Slots either list explicit item [LoadoutSlot.ids] or reference a [Gear] list to pick the best usable item from.
 * [inventoryGear] are [Gear] lists of tools carried in the inventory (e.g. pickaxes), picked by the levels needed to use them.
 * Defined in `*.loadouts.toml` or inline in a template's `loadouts` (hybrid bots).
 */
data class Loadout(
    val id: String,
    val slots: Map<EquipSlot, LoadoutSlot>,
    val inventory: List<BotItem> = emptyList(),
    val autocast: String? = null,
    val weight: Int = 1,
    val inventoryGear: List<LoadoutSlot> = emptyList(),
) {

    /**
     * Every candidate item for each slot, used by hybrid swaps and kit grants which don't select per bot.
     */
    val equipment: BotEquipmentSetup by lazy {
        BotEquipmentSetup(slots.mapValues { (_, slot) -> slot.item(slot.candidates.toSet()) })
    }

    val extraInventory: BotInventorySetup?
        get() = if (inventory.isEmpty()) null else BotInventorySetup(inventory)

    /**
     * For each required gear slot, the distinct requirements of its items (any one of which needs meeting).
     * Slots containing an item without requirements can always be met so are left out.
     */
    private val requirements: List<List<List<Pair<Skill, Int>>>> by lazy {
        slots.values.mapNotNull { slot -> options(slot) { it.requirements } } +
            inventoryGear.mapNotNull { slot -> options(slot) { it.useRequirements } }
    }

    private inline fun options(slot: LoadoutSlot, requirements: (GearItem) -> List<Pair<Skill, Int>>): List<List<Pair<Skill, Int>>>? {
        val gear = slot.gear
        if (slot.optional || gear == null) {
            return null
        }
        val options = gear.items.map(requirements).distinct()
        return if (options.any { it.isEmpty() }) null else options
    }

    /**
     * Skills which affect whether a bot [qualifies] for this loadout
     */
    val skills: Set<Skill>
        get() = requirements.flatMapTo(mutableSetOf()) { options ->
            options.flatMap { reqs -> reqs.map { it.first } }
        }

    /**
     * Whether [player] has the levels to equip at least one item in every required gear slot
     */
    fun qualifies(player: Player): Boolean {
        for (options in requirements) {
            if (options.none { reqs -> meets(player, reqs) }) {
                return false
            }
        }
        return true
    }

    /**
     * Raise [player]'s levels to the easiest requirements of each required gear slot
     */
    fun grantLevels(player: Player) {
        for (options in requirements) {
            if (options.any { meets(player, it) }) {
                continue
            }
            val easiest = options.minBy { reqs -> reqs.sumOf { (skill, level) -> (level - player.levels.getMax(skill)).coerceAtLeast(0) } }
            for ((skill, level) in easiest) {
                if (player.levels.getMax(skill) >= level) {
                    continue
                }
                player.experience.set(skill, Level.experience(skill, level))
                player.levels.set(skill, level)
            }
        }
    }

    /**
     * Pick the concrete items [player] should use for each slot.
     * Gear slots use the best item the player can equip and owns, otherwise required slots use the best they can [obtain].
     * Optional slots only use owned items (so a failed purchase can't block an activity) and are dropped otherwise.
     * Required slots with nothing owned or obtainable fall back to the best equippable item.
     */
    fun bind(player: Player, obtain: (id: String, amount: Int) -> Boolean): Map<EquipSlot, BotItem> = select(
        player,
        owned = { id, amount -> player.ownsItem(id, amount) },
        obtain = obtain,
    )

    /**
     * The items [player] should carry: the explicit [inventory] plus the best [inventoryGear] tools, picked like [bind]
     */
    fun bindInventory(player: Player, obtain: (id: String, amount: Int) -> Boolean): List<BotItem> = inventory + selectInventory(
        player,
        owned = { id, amount -> player.ownsItem(id, amount) },
        obtain = obtain,
    )

    /**
     * Every item which could be carried, with all candidates for each [inventoryGear] tool
     */
    val inventoryCandidates: List<BotItem>
        get() = inventory + inventoryGear.map { it.item(it.candidates.toSet()) }

    /**
     * Give [player] the best item they can equip for every slot (including optional ones) plus the loadout's inventory, all in the bank.
     */
    fun grant(player: Player) {
        for ((_, item) in select(player, fillOptional = true, owned = { _, _ -> true }, obtain = { _, _ -> false })) {
            val id = item.ids.firstOrNull { it != "empty" } ?: continue
            player.bank.add(id, item.min ?: 1)
        }
        for ((ids, min) in inventory + selectInventory(player, fillOptional = true, owned = { _, _ -> true }, obtain = { _, _ -> false })) {
            val id = ids.firstOrNull { it != "empty" } ?: continue
            player.bank.add(id, min ?: 1)
        }
    }

    /**
     * Wear the best item [player] can equip for every slot (including optional ones) and add the loadout's inventory.
     * @return false if the items didn't fit
     */
    fun equip(player: Player): Boolean {
        val selected = select(player, fillOptional = true, owned = { _, _ -> true }, obtain = { _, _ -> false })
        val worn = player.equipment.transaction {
            for ((slot, item) in selected) {
                val id = item.ids.firstOrNull { it != "empty" } ?: continue
                set(slot.index, Item(id, item.min ?: 1))
            }
        }
        val items = inventory + selectInventory(player, fillOptional = true, owned = { _, _ -> true }, obtain = { _, _ -> false })
        val carried = player.inventory.transaction {
            for ((ids, min) in items) {
                val id = ids.firstOrNull { it != "empty" } ?: continue
                add(id, min ?: 1)
            }
        }
        return worn && carried
    }

    private inline fun select(
        player: Player,
        fillOptional: Boolean = false,
        owned: (String, Int) -> Boolean,
        obtain: (String, Int) -> Boolean,
    ): Map<EquipSlot, BotItem> {
        val selected = LinkedHashMap<EquipSlot, BotItem>(slots.size)
        val shield = slots[EquipSlot.Shield]
        val excludeTwoHanded = shield != null && !shield.optional
        var twoHanded = false
        // Weapon first as a two-handed weapon removes the shield
        for ((slot, spec) in slots.entries.sortedBy { if (it.key == EquipSlot.Weapon) 0 else 1 }) {
            val gear = spec.gear
            if (gear == null) {
                selected[slot] = spec.item(spec.ids)
                continue
            }
            if (slot == EquipSlot.Shield && twoHanded) {
                continue
            }
            val chosen = choose(spec, gear, fillOptional, owned, obtain) { item ->
                (!excludeTwoHanded || !item.twoHanded) && item.canEquip(player) && (!spec.usable || item.canUse(player))
            }
            if (chosen == null) {
                if (!spec.optional) {
                    // Nothing equippable; leave all candidates for the resolvers to fail on
                    selected[slot] = spec.item(gear.ids.toSet())
                }
                continue
            }
            if (slot == EquipSlot.Weapon && chosen.twoHanded) {
                twoHanded = true
            }
            selected[slot] = spec.item(setOf(chosen.id))
        }
        return selected
    }

    /**
     * Pick a concrete item for each [inventoryGear] tool by the levels needed to use it rather than equip it
     */
    private inline fun selectInventory(
        player: Player,
        fillOptional: Boolean = false,
        owned: (String, Int) -> Boolean,
        obtain: (String, Int) -> Boolean,
    ): List<BotItem> {
        val selected = ArrayList<BotItem>(inventoryGear.size)
        for (spec in inventoryGear) {
            val gear = spec.gear ?: continue
            val chosen = choose(spec, gear, fillOptional, owned, obtain) { item ->
                item.canUse(player) && (!spec.equippable || item.canEquip(player))
            }
            if (chosen != null) {
                selected.add(spec.item(setOf(chosen.id)))
            } else if (!spec.optional) {
                // Nothing usable; leave all candidates for the resolvers to fail on
                selected.add(spec.item(gear.ids.toSet()))
            }
        }
        return selected
    }

    /**
     * The best [eligible] item in [gear] which is owned, otherwise the best obtainable (required slots only), otherwise the best [eligible].
     * Optional slots only use owned items unless [fillOptional].
     */
    private inline fun choose(
        spec: LoadoutSlot,
        gear: Gear,
        fillOptional: Boolean,
        owned: (String, Int) -> Boolean,
        obtain: (String, Int) -> Boolean,
        eligible: (GearItem) -> Boolean,
    ): GearItem? {
        val amount = spec.min ?: 1
        var best: GearItem? = null
        var obtainable: GearItem? = null
        for (item in gear.items) {
            if (!eligible(item)) {
                continue
            }
            if (best == null) {
                best = item
            }
            if (owned(item.id, amount)) {
                return item
            }
            if (obtainable == null && !spec.optional && obtain(item.id, amount)) {
                obtainable = item
            }
        }
        return obtainable ?: if (spec.optional && !fillOptional) null else best
    }

    private fun meets(player: Player, requirements: List<Pair<Skill, Int>>): Boolean {
        for ((skill, level) in requirements) {
            if (player.levels.getMax(skill) < level) {
                return false
            }
        }
        return true
    }

    /**
     * Check gear slots reference gear for the same equipment slot
     */
    fun validate() {
        if (!ItemDefinitions.loaded) {
            return
        }
        for ((slot, spec) in slots) {
            val gear = spec.gear ?: continue
            for ((id1, slot1) in gear.items) {
                require(slot1 == slot) { "Loadout '$id' slot ${slot.name.lowercase()} references gear '${gear.id}' with ${slot1.name.lowercase()} item '$id1'." }
            }
        }
    }

    override fun toString() = "Loadout($id)"

    companion object {
        /**
         * Parse a loadout from a map of equipment slot names to items, optionally nested inside an `equipment` map.
         * Slot values accept `id` or `gear` along with `min`, `max`, `optional`, `usable` and `equippable`.
         */
        @Suppress("UNCHECKED_CAST")
        fun parse(id: String, map: Map<String, Any>, gear: (String) -> Gear?): Loadout {
            val slots = LinkedHashMap<EquipSlot, LoadoutSlot>()
            var inventory: List<BotItem> = emptyList()
            var inventoryGear: List<LoadoutSlot> = emptyList()
            var autocast: String? = null
            var weight = 1
            fun slot(key: String, value: Any?) {
                val slot = EquipSlot.by(key)
                require(slot != EquipSlot.None) { "Invalid equipment slot '$key' in loadout '$id'." }
                val spec = value as? Map<String, Any> ?: error("Loadout '$id' slot '$key' expecting map, found: $value")
                slots[slot] = LoadoutSlot.parse(spec, gear) { "loadout '$id' slot '$key'" }
            }
            for ((key, value) in map) {
                when (key) {
                    "inventory" -> {
                        val list = when (value) {
                            is Map<*, *> -> listOf(value as Map<String, Any>)
                            is List<*> -> value as List<Map<String, Any>>
                            else -> error("Loadout '$id' inventory expecting list, found: $value")
                        }
                        val (tools, items) = list.partition { it.containsKey("gear") }
                        inventoryGear = tools.map { LoadoutSlot.parse(it, gear) { "loadout '$id' inventory" } }
                        if (items.isNotEmpty()) {
                            inventory = (Condition.parse(listOf("inventory" to items), "loadout $id").single() as BotInventorySetup).items
                        }
                    }
                    "autocast" -> autocast = value as String
                    "weight" -> weight = value as Int
                    "equipment" -> for ((slotKey, slotValue) in value as Map<String, Any>) {
                        slot(slotKey, slotValue)
                    }
                    else -> slot(key, value)
                }
            }
            return Loadout(id, slots, inventory, autocast, weight, inventoryGear)
        }
    }
}
