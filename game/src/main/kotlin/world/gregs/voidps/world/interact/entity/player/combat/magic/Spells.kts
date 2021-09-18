package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.activity.combat.prayer.getPrayerBonus
import world.gregs.voidps.world.interact.entity.combat.*
import kotlin.math.floor

on<HitEffectiveLevelOverride>({ type == "spell" && defence && target is NPC }, priority = Priority.HIGH) { _: Character ->
    level = (target as NPC).levels.get(Skill.Magic)
}

on<HitEffectiveLevelOverride>({ type == "spell" && defence && target is Player }, priority = Priority.LOW) { _: Character ->
    target as Player
    var level = floor(target.levels.get(Skill.Magic) * target.getPrayerBonus(Skill.Magic))
    level = floor(level * 0.7)
    this.level = (floor(this.level * 0.3) + level).toInt()
}

on<InterfaceOption>({ name.endsWith("_spellbook") && component == "defensive_cast" && option == "Defensive Casting" }) { player: Player ->
    player.toggleVar(component)
}

on<InterfaceOption>({ name.endsWith("_spellbook") && option == "Autocast" }) { player: Player ->
    val value = when {
        component.endsWith("_strike") -> 3
        component.endsWith("_bolt") -> 11
        component.endsWith("_blast") -> 19
        component.endsWith("_wave") -> 27
        component.endsWith("_surge") -> 47
        else -> 0
    }
    val type = when {
        component.startsWith("wind") -> 0
        component.startsWith("water") -> 2
        component.startsWith("earth") -> 4
        component.startsWith("fire") -> 6
        component == "crumble_undead" -> 35
        component == "magic_dart" -> 37
        component == "claws_of_guthix" -> 39
        component == "saradomin_strike" -> 41 - 3
        component == "flames_of_zamorak" -> 43
        component == "iban_blast" -> 45 - 19
        else -> 0
    }
    if (player.getVar<Int>("autocast") == value + type) {
        player.clearVar("autocast")
    } else {
        player["autocast"] = component
        player["attack_range"] = 8
        player.setVar("autocast", value + type)
    }
}

on<VariableSet>({ key == "autocast" && to == false }) { player: Player ->
    player.clear("autocast")
    player["attack_range"] = player.weapon.def["attack_range", 1]
}

on<CombatHit>({ spell.isNotBlank() }) { character: Character ->
    character.setGraphic("${spell}_hit", height = if (spell == "flames_of_zamorak" || spell == "teleport_block") 0 else 100)
}

on<CombatSwing>({ (delay ?: -1) >= 0 && it.spell.isNotBlank() }, Priority.LOWEST) { character: Character ->
    character.clear("spell")
    character.clear("spell_damage")
    character.clear("spell_experience")
    if (character is Player && !character.contains("autocast")) {
        character["attack_range"] = character.weapon.def["attack_range", 1]
        character.action.cancel(ActionType.Combat)
    }
}


val definitions: InterfaceDefinitions by inject()
val itemDefs: ItemDefinitions by inject()

on<CombatSwing>({ it.spell.isNotBlank() }, Priority.HIGHER) { player: Player ->
    val definition = definitions.get("modern_spellbook")// TODO other spellbooks
    val component = definition.getComponentOrNull(player.spell) ?: return@on
    val array = component.anObjectArray4758 ?: return@on
    val magicLevel = array[5] as Int

    if (!player.has(Skill.Magic, magicLevel, message = true)) {
        delay = -1
        return@on
    }
    val items = mutableListOf<Item>()
    array.forEach { id, amount ->
        if (id != -1 && amount > 0 && !hasRunes(player, id, amount, items)) {
            delay = -1
            player.clearVar("autocast")
            player.message("You do not have the required items to cast this spell.")
            return@on
        }
    }
    for (rune in items) {
        player.inventory.remove(rune.name, rune.amount)
    }
}

// TODO dungeoneering runes
fun hasRunes(player: Player, id: Int, amount: Int, items: MutableList<Item>): Boolean {
    val name = itemDefs.getName(id)

    if (hasInfiniteRunesEquipped(player, name, EquipSlot.Weapon)) {
        return true
    }
    if (hasInfiniteRunesEquipped(player, name, EquipSlot.Shield)) {
        return true
    }

    if (name.endsWith("_staff") && player.equipped(EquipSlot.Weapon).name == name) {
        return true
    }

    var remaining = amount
    var index = player.inventory.indexOf(name)
    if (index != -1) {
        val found = player.inventory.getAmount(index)
        if (found > 0) {
            items.add(Item(name, remaining.coerceAtMost(found)))
            remaining -= found
        }
    }

    if (remaining <= 0) {
        return true
    }

    val combinations = itemDefs.get(id).getOrNull("combination") as? ArrayList<String>
    if (combinations != null) {
        for (combination in combinations) {
            index = player.inventory.indexOf(combination)
            if (index != -1) {
                val found = player.inventory.getAmount(index)
                if (found > 0) {
                    items.add(Item(name, remaining.coerceAtMost(found)))
                    remaining -= found
                }
                if (remaining <= 0) {
                    return true
                }
            }
        }
    }
    return remaining <= 0
}

fun hasInfiniteRunesEquipped(player: Player, name: String, slot: EquipSlot): Boolean {
    val runes = player.equipped(slot).def.getOrNull("infinite") as? ArrayList<String>
    if (runes != null) {
        for (rune in runes) {
            if (name == rune) {
                return true
            }
        }
    }
    return false
}

inline fun Array<Any>.forEach(block: (Int, Int) -> Unit) {
    for (i in 8..14 step 2) {
        val id = this[i] as Int
        val amount = this[i + 1] as Int
        block.invoke(id, amount)
    }
}