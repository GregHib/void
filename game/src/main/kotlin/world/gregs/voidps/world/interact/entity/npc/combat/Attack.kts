package world.gregs.voidps.world.interact.entity.npc.combat

import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.npcCombatSwing
import world.gregs.voidps.world.interact.entity.sound.playSound

val definitions: WeaponStyleDefinitions by inject()
val animationDefinitions: AnimationDefinitions by inject()
val soundDefinitions: SoundDefinitions by inject()

npcCombatSwing { npc ->
    if (npc.tile.distanceTo(target) > npc.def["attack_radius", 8]) {
        cancel()
        npc.mode = Retreat(npc, target)
        return@npcCombatSwing
    }
    npc.setAnimation(attackAnimation(npc))
    (target as? Player)?.playSound(attackSound(npc))
    npc.hit(target)
}

fun attackAnimation(npc: NPC): String {
    var animation: String
    if (npc.def.contains("weapon_style")) {
        val id = npc.def["weapon_style", "unarmed"]
        val styleDefinition = definitions.get(id)
        var style = styleDefinition.combatStyles.indexOf(npc.def["style"])
        if (style == -1) {
            style = 0
        }

        animation = "${styleDefinition.stringId}_${styleDefinition.attackTypes[style]}"
        if (animationDefinitions.contains(animation)) {
            return animation
        }
    }
    animation = "${npc.id}_attack"
    if (animationDefinitions.contains(animation)) {
        return animation
    }
    if (npc.def.contains("attack_anim")) {
        animation = npc.def["attack_anim", ""]
        if (animationDefinitions.contains(animation)) {
            return animation
        }
    }
    if (npc.race.isNotEmpty()) {
        animation = "${npc.race}_attack"
        if (animationDefinitions.contains(animation)) {
            return animation
        }
    }
    return ""
}

fun attackSound(npc: NPC): String {
    var sound: String
    if (npc.def.contains("attack_sound")) {
        sound = npc.def["attack_sound"]
        if (soundDefinitions.contains(sound)) {
            return sound
        }
    }
    if (npc.race.isNotEmpty()) {
        sound = "${npc.race}_attack"
        if (soundDefinitions.contains(sound)) {
            return sound
        }
    }
    sound = "${npc.id}_attack"
    if (soundDefinitions.contains(sound)) {
        return sound
    }
    return ""
}