package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.utility.get

data class Animation(
    var first: Int = -1,
    var second: Int = -1,
    var third: Int = -1,
    var fourth: Int = -1,
    var speed: Int = 0
) : Visual {
    override fun needsReset(character: Character): Boolean {
        return first != -1
    }

    override fun reset(character: Character) {
        first = -1
        second = -1
        third = -1
        fourth = -1
        speed = 0
    }
}

const val PLAYER_ANIMATION_MASK = 0x8

const val NPC_ANIMATION_MASK = 0x8

private fun mask(character: Character) = if (character is Player) PLAYER_ANIMATION_MASK else NPC_ANIMATION_MASK

fun Character.flagAnimation() = visuals.flag(mask(this))

fun Character.getAnimation() = visuals.getOrPut(mask(this)) { Animation() }

fun Character.setAnimation(id: Int, speed: Int = 0) {
    setAnimation(getAnimation(), id, speed)
    flagAnimation()
}

fun Character.clearAnimation() = setAnimation(-1)

fun Character.setAnimation(name: String, speed: Int = 0) {
    setAnimation(get<AnimationDefinitions>().getIdOrNull(name) ?: return, speed)
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