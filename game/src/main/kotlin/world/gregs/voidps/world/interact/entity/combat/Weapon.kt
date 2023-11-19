package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.weaponStyle
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell

object Weapon {

    fun isBowOrCrossbow(item: Item) = item.id.endsWith("bow") || item.id == "seercull" || item.id.endsWith("longbow_sighted")

    fun type(character: Character, weapon: Item = character.weapon): String {
        if (character.spell.isNotBlank()) {
            return "magic"
        }
        return when (weapon.def.weaponStyle()) {
            13, 16, 17, 18, 19 -> "range"
            20 -> if (character.attackType == "aim_and_fire") "range" else "melee"
            21 -> when (character.attackType) {
                "flare" -> "range"
                "blaze" -> "blaze"
                else -> "melee"
            }
            else -> "melee"
        }
    }

    fun strengthBonus(source: Character, type: String, weapon: Item?) = when {
        type == "blaze" -> weapon?.def?.getOrNull("blaze_str") ?: 0
        type == "range" && source is Player && weapon != null && (weapon.id == source.ammo || !Ammo.required(weapon)) -> weapon.def["range_str", 0]
        else -> source[if (type == "range") "range_str" else "str", 0]
    } + 64
}

val Character.fightStyle: String
    get() = Weapon.type(this)

var Character.weapon: Item
    get() = get("weapon", Item.EMPTY)
    set(value) = set("weapon", value)

var Character.attackRange: Int
    get() = get("attack_range", if (this is NPC) def["attack_range", 1] else 1)
    set(value) = set("attack_range", value)

// E.g "accurate"
val Character.attackStyle: String
    get() = get("attack_style", "")

// E.g "flick"
val Character.attackType: String
    get() = get("attack_type", "")

// E.g "crush"
val Character.combatStyle: String
    get() = get("combat_style", "")