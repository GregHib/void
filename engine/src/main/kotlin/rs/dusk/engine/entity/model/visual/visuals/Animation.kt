package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Animation(
    var first: Int = -1,
    var second: Int = -1,
    var third: Int = -1,
    var fourth: Int = -1,
    var speed: Int = 0
) : Visual

const val PLAYER_ANIMATION_MASK = 0x40

const val NPC_ANIMATION_MASK = 0x10

fun Player.flagAnimation() = visuals.flag(PLAYER_ANIMATION_MASK)

fun NPC.flagAnimation() = visuals.flag(NPC_ANIMATION_MASK)

fun Player.getAnimation() = visuals.getOrPut(PLAYER_ANIMATION_MASK) { Animation() }

fun NPC.getAnimation() = visuals.getOrPut(NPC_ANIMATION_MASK) { Animation() }

fun Player.setAnimation(id: Int, speed: Int = 0) {
    setAnimation(getAnimation(), id, speed)
    flagAnimation()
}

fun NPC.setAnimation(id: Int, speed: Int = 0) {
    setAnimation(getAnimation(), id, speed)
    flagAnimation()
}

private fun setAnimation(anim: Animation, id: Int, speed: Int) {
    when {
        anim.first == -1 -> {
            anim.first = id
            anim.speed = speed
        }
        anim.second == -1 -> anim.second = id
        anim.third == -1 -> anim.third = id
        anim.fourth == -1 -> anim.fourth = id
    }
}