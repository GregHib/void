package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.utility.get

/**
 * @param stand animate only while stationary (or during force movement)
 * @param force animate after force movement
 * @param walk can animate while walking
 * @param run can animate while running
 */
data class Animation(
    var stand: Int = -1,
    var force: Int = -1,
    var walk: Int = -1,
    var run: Int = -1,
    var speed: Int = 0
) : Visual {
    var priority: Int = -1

    override fun needsReset(character: Character): Boolean {
        return stand != -1 || force != -1 || walk != -1 || run != -1
    }

    override fun reset(character: Character) {
        stand = -1
        force = -1
        walk = -1
        run = -1
        speed = 0
        priority = -1
    }
}

const val PLAYER_ANIMATION_MASK = 0x8

const val NPC_ANIMATION_MASK = 0x8

private fun mask(character: Character) = if (character is Player) PLAYER_ANIMATION_MASK else NPC_ANIMATION_MASK

fun Character.flagAnimation() = visuals.flag(mask(this))

fun Character.getAnimation() = visuals.getOrPut(mask(this)) { Animation() }

fun Character.setAnimation(name: String, override: Boolean = false): Long {
    val definition = get<AnimationDefinitions>().getOrNull(name) ?: return -1
    return setAnimation(definition.id, definition["speed", 0], override, definition["stand", true], definition["force", true], definition["walk", true], definition["run", true])
}

fun Character.setAnimation(id: Int, speed: Int = 0, override: Boolean = false, stand: Boolean = true, force: Boolean = true, walk: Boolean = true, run: Boolean = true): Long {
    val definition = get<AnimationDefinitions>().get(id)
    val priority = definition.priority
    val anim = getAnimation()
    if (!override && hasEffect("animation_delay") && priority < anim.priority) {
        return -1
    }
    start("animation_delay", 1)
    if (stand) {
        anim.stand = id
    }
    if (force) {
        anim.force = id
    }
    if (walk) {
        anim.walk = id
    }
    if (run) {
        anim.run = id
    }
    if (stand || force || walk || run) {
        anim.speed = speed
        anim.priority = priority
    }
    flagAnimation()
    return definition.time
}

fun Character.clearAnimation() {
    getAnimation().reset(this)
    flagAnimation()
}