package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Indexed
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

fun Player.flagAnimation() = visuals.flag(0x40)

fun NPC.flagAnimation() = visuals.flag(0x10)

fun Indexed.flagAnimation() {
    if (this is Player) flagAnimation() else if (this is NPC) flagAnimation()
}

fun Indexed.getAnimation() = visuals.getOrPut(Animation::class) { Animation() }

fun Indexed.setAnimation(id: Int, speed: Int) {
    val anim = Animation()
    when {
        anim.first == -1 -> {
            anim.first = id
            anim.speed = speed
        }
        anim.second == -1 -> anim.second = id
        anim.third == -1 -> anim.third = id
        anim.fourth == -1 -> anim.fourth = id
    }
    flagAnimation()
}