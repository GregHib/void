package content.skill.magic.spell

import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.Transaction
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import kotlin.math.max

object SpellRunes {

    fun Transaction.removeItems(player: Player, spell: String, message: Boolean = true) {
        val component = get<InterfaceDefinitions>().getComponent(player.spellBook, spell)
        if (component == null || !player.has(Skill.Magic, component.magicLevel, message = message)) {
            error = TransactionError.Deficient(0)
            return
        }
        val required = component.spellRequiredItems()
        if (required == null) {
            error = TransactionError.Deficient(0)
            return
        }
        checkInfiniteStaff(player, required, "air")
        checkInfiniteStaff(player, required, "water")
        checkInfiniteStaff(player, required, "earth")
        checkInfiniteStaff(player, required, "fire")

        dungeoneeringMagicBoxes(player, required, spell)
        checkMinigame(player, required)
        if (World.members) {
            removeCombo(required, "mist_rune", "air_rune", "water_rune")
            removeCombo(required, "dust_rune", "air_rune", "earth_rune")
            removeCombo(required, "mud_rune", "water_rune", "earth_rune")
            removeCombo(required, "smoke_rune", "air_rune", "fire_rune")
            removeCombo(required, "steam_rune", "water_rune", "fire_rune")
            removeCombo(required, "lava_rune", "earth_rune", "fire_rune")
        }
        dungeoneeringStaffCharges(required, player, message)
        checkStaves(player, required)
        // Regular runes
        required.keys.removeIf { key ->
            if (!key.endsWith("_rune")) {
                false
            } else {
                remove(key, required.getValue(key))
                !failed
            }
        }
        if (required.isNotEmpty()) {
            error = TransactionError.Deficient(required.values.first())
            if (message) {
                player.message("You do not have the required items to cast this spell.")
            }
        }
    }

    private fun Transaction.dungeoneeringStaffCharges(required: MutableMap<String, Int>, player: Player, message: Boolean) {
        if (required.containsKey("nature_rune") && player.equipped(EquipSlot.Weapon).id == "nature_staff") {
            val charges = player.equipment.charges(player, EquipSlot.Weapon.index)
            val amount = required.dec("nature_rune", charges)
            if (random.nextInt(10) == 0 && message) {
                player.message("Your staff magically pays for the cost of the nature rune.", ChatType.Filter)
            } else if (player.equipment.discharge(player, EquipSlot.Weapon.index, amount)) {
                state.onRevert { player.equipment.charge(player, EquipSlot.Weapon.index, amount) }
            }
        } else if (required.containsKey("law_rune") && player.equipped(EquipSlot.Weapon).id == "law_staff") {
            val charges = player.equipment.charges(player, EquipSlot.Weapon.index)
            val amount = required.dec("law_rune", charges)
            if (random.nextInt(10) == 0 && message) {
                player.message("Your staff magically pays for the cost of the law rune.", ChatType.Filter)
            } else if (player.equipment.discharge(player, EquipSlot.Weapon.index, amount = amount)) {
                state.onRevert { player.equipment.charge(player, EquipSlot.Weapon.index, amount = amount) }
            }
        }
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

    private fun Transaction.dungeoneeringMagicBoxes(player: Player, required: MutableMap<String, Int>, spell: String) {
        if (removeBoxCharges(player, spell, "magical_blastbox", "_bolt")) {
            required.remove("air_rune")
            required.remove("chaos_rune")
        } else if (removeBoxCharges(player, spell, "magical_blastbox", "_blast")) {
            required.remove("air_rune")
            required.remove("death_rune")
        } else if (removeBoxCharges(player, spell, "celestial_surgebox", "_wave")) {
            required.remove("air_rune")
            required.remove("blood_rune")
        } else if (removeBoxCharges(player, spell, "celestial_surgebox", "_surge")) {
            required.remove("air_rune")
            required.remove("death_rune")
            required.remove("blood_rune")
        }
    }

    private fun Transaction.removeBoxCharges(player: Player, spell: String, box: String, suffix: String): Boolean {
        if (spell.endsWith(suffix) && player.equipped(EquipSlot.Shield).id.startsWith(box)) {
            if (player.equipment.discharge(player, EquipSlot.Shield.index)) {
                state.onRevert { player.equipment.charge(player, EquipSlot.Shield.index) }
                return true
            }
        }
        return false
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
        val elemental = inventory.count("elemental_rune")
        val catalyst = inventory.count("catalytic_rune")
        var count = 0
        for (rune in elementalRunes) {
            if (required.containsKey(rune) && count < elemental) {
                count += required.dec(rune, elemental)
            }
        }
        remove("elemental_rune", count)
        count = 0
        for (rune in catalyticRunes) {
            if (!World.members && rune == "soul_rune") {
                break
            }
            if (required.containsKey(rune) && count < catalyst) {
                count += required.dec(rune, catalyst)
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

    private fun Transaction.removeCombo(required: MutableMap<String, Int>, combo: String, element1: String, element2: String) {
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

fun Player.hasSpellItems(spell: String, message: Boolean = true): Boolean {
    val transaction = inventory.transaction
    transaction.start()
    transaction.removeItems(this, spell, message)
    val success = !transaction.failed
    transaction.revert()
    return success
}

fun Player.removeSpellItems(spell: String) = inventory.transaction {
    removeItems(this@removeSpellItems, spell)
}
