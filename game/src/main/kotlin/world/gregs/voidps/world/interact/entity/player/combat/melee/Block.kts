package world.gregs.voidps.world.interact.entity.player.combat.melee

import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.sound.playSound

val styleDefinitions: WeaponStyleDefinitions by inject()
val weaponDefinitions: WeaponAnimationDefinitions by inject()
val animationDefinitions: AnimationDefinitions by inject()

characterCombatAttack { character ->
    character.playSound(calculateHitSound(target), delay)
    if (target is Player) {
        target.playSound(calculateHitSound(target), delay)
        val shield = target.equipped(EquipSlot.Shield).id
        if (shield.endsWith("shield")) {
            target.setAnimation("shield_block", delay)
        } else if (shield.endsWith("defender")) {
            target.setAnimation("defender_block", delay)
        } else if (shield.endsWith("book")) {
            target.setAnimation("book_block", delay)
        } else {
            val type: String? = target.weapon.def.getOrNull("weapon_type")
            val definition = if (type != null) weaponDefinitions.get(type) else null
            var animation = definition?.attackTypes?.getOrDefault(target.attackType, definition.attackTypes["default"])
            if (animation == null) {
                val id = target.weapon.def["weapon_style", -1]
                val style = styleDefinitions.get(id)
                animation = if (id != -1 && animationDefinitions.contains("${style.stringId}_hit")) "${style.stringId}_hit" else "human_hit"
            }
            target.setAnimation(animation, delay)
        }
        blocked = true
    } else if (target is NPC) {
        val animation = if (target.race.isNotEmpty()) "${target.race}_hit" else target.def.getOrNull("hit_anim") ?: return@characterCombatAttack
        target.setAnimation(animation, delay)
        blocked = true
    }
}

fun calculateHitSound(target: Character): String {
    if (target is NPC) {
        return "${target.race}_hit"
    }

    if (target is Player) {
        return if (target.male) {
            "male_hit_${random.nextInt(0, 3)}"
        } else {
            "female_hit_${random.nextInt(0, 1)}"
        }
    }
    return "human_hit"
}