package content.skill.melee

import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import content.skill.slayer.race
import content.entity.combat.hit.characterCombatAttack
import content.skill.melee.weapon.weapon
import content.entity.sound.playSound

val styleDefinitions: WeaponStyleDefinitions by inject()
val weaponDefinitions: WeaponAnimationDefinitions by inject()
val animationDefinitions: AnimationDefinitions by inject()
val soundDefinitions: SoundDefinitions by inject()

characterCombatAttack { character ->
    character.playSound(calculateHitSound(target), delay)
    if (target is Player) {
        target.playSound(calculateHitSound(target), delay)
        val shield = target.equipped(EquipSlot.Shield).id
        if (shield.endsWith("shield")) {
            target.anim("shield_block", delay)
        } else if (shield.endsWith("defender")) {
            target.anim("defender_block", delay)
        } else if (shield.endsWith("book")) {
            target.anim("book_block", delay)
        } else {
            val type: String? = target.weapon.def.getOrNull("weapon_type")
            val definition = if (type != null) weaponDefinitions.get(type) else null
            var animation = definition?.attackTypes?.get("defend")
            if (animation == null) {
                val id = target.weapon.def["weapon_style", -1]
                val style = styleDefinitions.get(id)
                animation = if (id != -1 && animationDefinitions.contains("${style.stringId}_hit")) "${style.stringId}_hit" else "human_defend"
            }
            target.anim(animation, delay)
        }
    } else if (target is NPC) {
        val animation = hitAnimation(target)
        target.anim(animation, delay)
    }
}

fun hitAnimation(npc: NPC): String {
    var animation = "${npc.id}_defend"
    if (animationDefinitions.contains(animation)) {
        return animation
    }
    if (npc.def.contains("defend_anim")) {
        animation = npc.def["defend_anim", ""]
        if (animationDefinitions.contains(animation)) {
            return animation
        }
    }
    if (npc.race.isNotEmpty()) {
        animation = "${npc.race}_defend"
        if (animationDefinitions.contains(animation)) {
            return animation
        }
    }
    return ""
}

fun calculateHitSound(target: Character): String {
    if (target is NPC) {
        var sound: String
        if (target.def.contains("hit_sound")) {
            sound = target.def["hit_sound"]
            if (soundDefinitions.contains(sound)) {
                return sound
            }
        }
        sound = "${target.id}_hit"
        if (soundDefinitions.contains(sound)) {
            return sound
        }
        if (target.race.isNotEmpty()) {
            sound = "${target.race}_hit"
            if (soundDefinitions.contains(sound)) {
                return sound
            }
        }
        return ""
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