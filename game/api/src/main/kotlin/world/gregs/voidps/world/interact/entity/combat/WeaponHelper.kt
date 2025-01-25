package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

object WeaponHelper {
    fun type(character: Character, weapon: Item = character.weapon): String {
        if (character.spell.isNotBlank()) {
            return "magic"
        }
        val definitions = get<WeaponStyleDefinitions>()
        val style = if (character is NPC) definitions.get(character.def["weapon_style", "unarmed"]) else definitions.get(weapon.def["weapon_style", 0])
        return when (style.stringId) {
            "pie", "bow", "crossbow", "thrown", "chinchompa", "sling" -> "range"
            "fixed_device" -> if (character.attackType == "aim_and_fire") "range" else "melee"
            "salamander" -> when (character.attackType) {
                "blaze" -> "blaze"
                "scorch" -> "scorch"
                else -> "range"
            }
            else -> "melee"
        }
    }

}

val Character.fightStyle: String
    get() = WeaponHelper.type(this)

var Character.weapon: Item
    get() = get("weapon", Item.EMPTY)
    set(value) = set("weapon", value)

val Character.attackSpeed: Int
    get() = when {
        this is NPC -> def["attack_speed", 4]
        fightStyle == "magic" -> 5
        this is Player && specialAttack && weapon.id.startsWith("granite_maul") -> 1
        else -> weapon.def["attack_speed", 4] - (attackType == "rapid" || attackType == "medium_fuse").toInt()
    }

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

