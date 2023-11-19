package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.weaponStyle

object Weapon {
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
}

val Character.fightStyle: String
    get() = Weapon.type(this)

var Character.weapon: Item
    get() = get("weapon", Item.EMPTY)
    set(value) = set("weapon", value)