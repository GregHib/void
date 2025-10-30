package content.skill.melee

import content.entity.combat.combatPrepare
import content.entity.combat.combatSwing
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
        combatPrepare("melee") { player ->
            if (player.specialAttack && !SpecialAttack.hasEnergy(player)) {
                cancel()
            }
        }

        combatSwing(style = "melee") { player ->
            if (player.specialAttack && SpecialAttack.drain(player)) {
                val id: String = player.weapon.def["special"]
                player.emit(SpecialAttack(id, target))
                return@combatSwing
            }
            val type: String? = player.weapon.def.getOrNull("weapon_type")
            val definition = if (type != null) animationDefinitions.get(type) else null
            var animation = definition?.attackTypes?.getOrDefault(player.attackType, definition.attackTypes["default"])
            if (animation == null) {
                val id = player.weapon.def["weapon_style", 0]
                val style = styleDefinitions.get(id)
                animation = "${style.stringId}_${player.attackType}"
            }
            player.anim(animation)
            player.hit(target)
        }
    }
}
