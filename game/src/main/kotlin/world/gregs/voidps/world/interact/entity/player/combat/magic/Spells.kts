package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
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
        player.clear("autocast")
        player.clearVar("autocast")
        player.weapon = player.equipped(EquipSlot.Weapon)
        player["attack_range"] = 1// FIXME if player already has bow or halberd equipped then this will be wrong
    } else {
        player["autocast"] = component
        player["attack_range"] = 8
        player.setVar("autocast", value + type)
    }
}

on<ItemChanged>({ !item.name.endsWith("staff") && oldItem.name.endsWith("staff") }) { player: Player ->
    player.clear("autocast")
    player.clearVar("autocast")
}

on<CombatHit>({ spell.isNotBlank() }) { character: Character ->
    character.setGraphic("${spell}_hit", height = if (spell == "flames_of_zamorak" || spell == "teleport_block") 0 else 100)
}

on<CombatSwing>({ it.spell.isBlank() }, Priority.HIGHER) { character: Character ->
    character.clear("spell_damage")
    character.clear("spell_experience")
}

on<CombatSwing>({ it.spell.isNotBlank() }, Priority.LOWEST) { character: Character ->
    character.clear("spell")
    if (character.spell.isBlank()) {
        character.clear("spell_damage")
        character.clear("spell_experience")
        character["attack_range"] = 1// FIXME should be 1 tick before a single spell switches back to melee and tries to run closer.
    }
}