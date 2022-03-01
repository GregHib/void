package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.visual.VisualMask.NPC_ANIMATION_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_ANIMATION_MASK

private fun mask(character: Character) = if (character is Player) PLAYER_ANIMATION_MASK else NPC_ANIMATION_MASK

fun Character.flagAnimation() = visuals.flag(mask(this))

fun Character.setAnimation(id: String, override: Boolean = false): Int {
    val definition = get<AnimationDefinitions>().getOrNull(id) ?: return -1
    val anim = visuals.animation
    if (!override && hasEffect("animation_delay") && definition.priority < anim.priority) {
        return -1
    }
    start("animation_delay", 1)
    val stand = definition["stand", true]
    if (stand) {
        anim.stand = definition.id
    }
    val force = definition["force", true]
    if (force) {
        anim.force = definition.id
    }
    val walk = definition["walk", true]
    if (walk) {
        anim.walk = definition.id
    }
    val run = definition["run", true]
    if (run) {
        anim.run = definition.id
    }
    if (stand || force || walk || run) {
        anim.speed = definition["speed", 0]
        anim.priority = definition.priority
    }
    flagAnimation()
    return definition["ticks", 0]
}

fun Character.clearAnimation() {
    visuals.animation.reset()
    flagAnimation()
}