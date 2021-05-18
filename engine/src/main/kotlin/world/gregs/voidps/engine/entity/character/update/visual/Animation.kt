package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.utility.get

data class Animation(
    var stand: Int = -1,
    var unknown: Int = -1,
    var walk: Int = -1,
    var run: Int = -1,
    var speed: Int = 0
) : Visual {
    var priority: Int = -1

    override fun needsReset(character: Character): Boolean {
        return stand != -1
    }

    override fun reset(character: Character) {
        stand = -1
        unknown = -1
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

/**
 * @param stand animate only while stationary
 * @param walk can animate while walking
 * @param run can animate while running
 */
fun Character.setAnimation(name: String, speed: Int = 0, stand: Boolean = true, walk: Boolean = true, run: Boolean = true): Long {
    return setAnimation(get<AnimationDefinitions>().getIdOrNull(name) ?: return -1, speed, stand, walk, run)
}

fun Character.setAnimation(id: Int, speed: Int = 0, stand: Boolean = true, walk: Boolean = true, run: Boolean = true): Long {
    val time = setAnimation(getAnimation(), id, speed, stand, walk, run)
    flagAnimation()
    return time
}

fun Character.clearAnimation() {
    getAnimation().reset(this)
    flagAnimation()
}

private fun setAnimation(anim: Animation, id: Int, speed: Int, stand: Boolean, walk: Boolean, run: Boolean): Long {
    val definition = get<AnimationDefinitions>().get(id)
    val priority = definition.priority
    if (priority <= anim.priority) {
        return -1
    }
    if (stand) {
        anim.stand = id
    }
    if (walk) {
        anim.walk = id
    }
    if (run) {
        anim.run = id
    }
    if (stand || walk || run) {
        anim.speed = speed
        anim.priority = priority
    }
    return definition.time
}