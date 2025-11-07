package content.skill.melee

import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttack
import content.skill.melee.weapon.attackType
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.inject

class Melee : Script {

    val styleDefinitions: WeaponStyleDefinitions by inject()
    val animationDefinitions: WeaponAnimationDefinitions by inject()

    init {
        combatPrepare("melee") {
            !(specialAttack && !SpecialAttack.hasEnergy(this))
        }

        combatSwing(style = "melee") { target ->
            if (specialAttack && SpecialAttack.drain(this)) {
                val id: String = weapon.def["special"]
                SpecialAttack.special(this, target, id)
                return@combatSwing
            }
            val type: String? = weapon.def.getOrNull("weapon_type")
            val definition = if (type != null) animationDefinitions.get(type) else null
            var animation = definition?.attackTypes?.getOrDefault(attackType, definition.attackTypes["default"])
            if (animation == null) {
                val id = weapon.def["weapon_style", 0]
                val style = styleDefinitions.get(id)
                animation = "${style.stringId}_$attackType"
            }
            anim(animation)
            hit(target)
        }
    }
}
