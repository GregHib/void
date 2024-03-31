package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.config.SpellDefinition
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.Transaction
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.Equipment
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

object Spell {

    fun isMultiTarget(spell: String) = spell.endsWith("_burst") || spell.endsWith("_barrage")

    fun canDrain(target: Character, spell: String): Boolean {
        val def = get<SpellDefinitions>().get(spell)
        val skill = Skill.valueOf(def["drain_skill"])
        val multiplier: Double = def["drain_multiplier"]
        val maxDrain = multiplier * target.levels.getMax(skill)
        return target.levels.getOffset(skill) > -maxDrain
    }

    fun canDrain(target: Character, def: SpellDefinition): Boolean {
        val skill = Skill.valueOf(def["drain_skill"])
        val multiplier: Double = def["drain_multiplier"]
        val maxDrain = multiplier * target.levels.getMax(skill)
        return target.levels.getOffset(skill) > -maxDrain
    }

    fun drain(source: Character, target: Character, spell: String) {
        val def = get<SpellDefinitions>().get(spell)
        val multiplier: Double = def["drain_multiplier"]
        val skill = Skill.valueOf(def["drain_skill"])
        val drained = target.levels.drain(skill, multiplier = multiplier, stack = target is Player)
        if (target.levels.getOffset(skill).absoluteValue >= multiplier * 100 && drained == 0) {
            source.message("The spell has no effect because the target has already been weakened.")
        } else {
            target.message("You feel slightly weakened.", ChatType.Filter)
        }
    }

    /**
     * Applies modifications to spells damage
     */
    fun damageModifiers(source: Character, type: String, weapon: Item, spell: String, baseDamage: Int): Int {
        if (type != "magic") {
            return baseDamage
        }
        var damage = baseDamage
        val magicDamage = weapon.def["magic_damage", 0]
        if (magicDamage > 0 || Equipment.hasEliteVoidEffect(source)) {
            val equipmentDamage = magicDamage / 100.0
            val eliteVoidDamage = if (Equipment.hasEliteVoidEffect(source)) 0.025 else 0.0
            val damageMultiplier = 1.0 + equipmentDamage + eliteVoidDamage
            damage = (damage * damageMultiplier).roundToInt()
        }
        if (source.hasClock("charge") && source is Player && Equipment.wearingMatchingArenaGear(source, spell)) {
            damage += 100
        }
        return damage
    }

    fun removeRequirements(player: Player, spell: String) = requirements(player, spell, remove = true)

    fun hasRequirements(player: Player, spell: String, runes: MutableList<Item> = mutableListOf()) = requirements(player, spell, remove = false)

    private fun requirements(player: Player, spell: String, remove: Boolean): Boolean {
        val component = get<InterfaceDefinitions>().getComponent(player.spellBook, spell) ?: return false
        if (!player.has(Skill.Magic, component.magicLevel, message = true)) {
            return false
        }
        val requiredItems = component.spellRequiredItems() ?: return false
        if (!hasRequired(player, spell, requiredItems, remove = remove)) {
            player.message("You do not have the required items to cast this spell.")
            return false
        }
        return true
    }


    private fun MutableMap<String, Int>.dec(key: String, value: Int): Int {
        val count = this[key] ?: return 0
        val max = value.coerceAtMost(count)
        val reduced = count - max
        if (reduced <= 0) {
            this.remove(key)
        } else {
            this[key] = reduced
        }
        return max
    }

    private fun removeBoxes(player: Player, required: MutableMap<String, Int>, spell: String, box: String, suffix: String, catalyst: String): Boolean {
        if (spell.endsWith(suffix) && player.equipped(EquipSlot.Shield).id.startsWith(box)) {
            val charges = Degrade.charges(player, "worn_equipment", EquipSlot.Shield.index, suffix)
            if (charges > 0) {
                required.remove("air_rune")
                required.remove(catalyst)
                return true
            }
        }
        return false
    }

    private fun hasRequired(player: Player, spell: String, required: MutableMap<String, Int>, remove: Boolean): Boolean {
        var slot = EquipSlot.None
        var degrade = 1
        var suffix = ""
        var success = false
        player.inventory.transaction {
            checkInfiniteStaff(player, required, "air")
            checkInfiniteStaff(player, required, "water")
            checkInfiniteStaff(player, required, "earth")
            checkInfiniteStaff(player, required, "fire")

            // Dungeoneering magic boxes
            if (removeBoxes(player, required, spell, "magical_blastbox", "_bolt", "chaos_rune")) {
                suffix = "_bolt"
                slot = EquipSlot.Shield
            } else if (removeBoxes(player, required, spell, "magical_blastbox", "_blast", "death_rune")) {
                suffix = "_blast"
                slot = EquipSlot.Shield
            } else if (removeBoxes(player, required, spell, "celestial_surgebox", "_wave", "blood_rune")) {
                suffix = "_wave"
                slot = EquipSlot.Shield
            } else if (removeBoxes(player, required, spell, "celestial_surgebox", "_surge", "death_rune")) {
                suffix = "_surge"
                slot = EquipSlot.Shield
                required.remove("blood_rune")
            }
            checkMinigame(player, required)
            if (World.members) {
                removeCombo(required, "mist_rune", "air_rune", "water_rune")
                removeCombo(required, "dust_rune", "air_rune", "earth_rune")
                removeCombo(required, "mud_rune", "water_rune", "earth_rune")
                removeCombo(required, "smoke_rune", "air_rune", "fire_rune")
                removeCombo(required, "steam_rune", "water_rune", "fire_rune")
                removeCombo(required, "lava_rune", "earth_rune", "fire_rune")
            }
            // Dungeoneering staves
            if (required.containsKey("nature_rune") && player.equipped(EquipSlot.Weapon).id == "nature_staff") {
                val charges = Degrade.charges(player, "worn_equipment", EquipSlot.Weapon.index)
                degrade = required.dec("nature_rune", charges)
                slot = EquipSlot.Weapon
            } else if (required.containsKey("law_rune") && player.equipped(EquipSlot.Weapon).id == "law_staff") {
                val charges = Degrade.charges(player, "worn_equipment", EquipSlot.Weapon.index)
                degrade = required.dec("law_rune", charges)
                slot = EquipSlot.Weapon
            }
            checkStaves(player, required)
            // Regular runes
            for ((key, amount) in required) {
                if (!key.endsWith("_rune")) {
                    continue
                }
                remove(key, amount)
            }
            if (required.isNotEmpty()) {
                error = TransactionError.Deficient(required.values.first())
            }
            if (!remove) {
                success = !failed
                error = TransactionError.Invalid
            }
        }
        if (remove && player.inventory.transaction.error == TransactionError.None && suffix.isEmpty()) {
            Degrade.discharge(player, "worn_equipment", slot.index, amount = degrade, suffix)
            success = true
        }
        return success
    }

    private fun checkInfiniteStaff(player: Player, required: MutableMap<String, Int>, element: String) {
        if (required.containsKey("${element}_rune") && player.equipped(EquipSlot.Weapon).def.contains("infinite_${element}_runes")) {
            required.remove("${element}_rune")
        }
    }

    private val elementalRunes = listOf("air_rune", "water_rune", "earth_rune", "fire_rune")
    private val catalyticRunes = listOf("body_rune", "mind_rune", "cosmic_rune", "chaos_rune", "nature_rune", "death_rune", "law_rune", "soul_rune", "blood_rune", "astral_rune")

    private fun Transaction.checkMinigame(player: Player, required: MutableMap<String, Int>) {
        val type = player["minigame_type", "none"]
        if (type != "stealing_creation" && type != "fist_of_guthix" && type != "barbarian_assault") {
            return
        }
        var elemental = player.inventory.count("elemental_rune")
        var count = 0
        for (rune in elementalRunes) {
            if (required.containsKey(rune) && elemental > 0) {
                elemental -= required.dec(rune, elemental)
                count++
            }
        }
        count = 0
        remove("elemental_rune", count)
        var catalyst = player.inventory.count("catalytic_rune")
        for (rune in catalyticRunes) {
            if (!World.members && rune == "soul_rune") {
                break
            }
            if (required.containsKey(rune) && catalyst > 0) {
                catalyst -= required.dec(rune, catalyst)
                count++
            }
        }
        remove("catalytic_rune", count)
    }

    private fun checkStaves(player: Player, required: MutableMap<String, Int>) {
        val weapon = player.equipped(EquipSlot.Weapon).id
        when {
            weapon == "guthix_staff" || weapon == "void_knight_mace" -> required.remove("guthix_staff_dummy")
            weapon == "slayers_staff" || weapon.startsWith("staff_of_light") -> required.remove("slayers_staff")
            weapon.startsWith("zuriels_staff") -> required.remove("zuriels_staff")
            weapon == "ibans_staff" -> required.remove("ibans_staff")
            weapon == "saradomin_staff" -> required.remove("saradomin_staff")
            weapon == "zamorak_staff" -> required.remove("zamorak_staff")
        }
    }

    private fun Transaction.removeCombo(
        required: MutableMap<String, Int>,
        combo: String,
        element1: String,
        element2: String
    ) {
        if (!inventory.contains(combo)) {
            return
        }
        if (!required.containsKey(element1) && !required.containsKey(element2)) {
            return
        }
        val count = inventory.count(combo)
        val removed = max(required.dec(element1, count), required.dec(element2, count))
        if (removed > 0) {
            remove(combo, removed)
        }
    }

    private val InterfaceComponentDefinition.magicLevel: Int
        get() = information?.getOrNull(5) as? Int ?: 0

    private fun InterfaceComponentDefinition.spellRequiredItems(): MutableMap<String, Int>? {
        val array = information ?: return null
        val map = mutableMapOf<String, Int>()
        val definitions: ItemDefinitions = get()
        for (i in 8..14 step 2) {
            val id = array[i] as Int
            val amount = array[i + 1] as Int
            if (id == -1 || amount <= 0) {
                break
            }
            val item = definitions.get(id)
            if (item.members && !World.members) {
                return null
            }
            map[item.stringId] = amount
        }
        return map
    }
}


var Character.spell: String
    get() = get("spell", get("autocast_spell", ""))
    set(value) = set("spell", value)

val Player.spellBook: String
    get() = interfaces.get("spellbook_tab") ?: "unknown_spellbook"