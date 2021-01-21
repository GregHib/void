package world.gregs.void.engine.entity.character.update.visual

import world.gregs.void.engine.entity.character.Character
import world.gregs.void.engine.entity.character.npc.NPC
import world.gregs.void.engine.entity.character.npc.NPCEvent
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.entity.character.update.Visual
import world.gregs.void.engine.entity.definition.AnimationDefinitions
import world.gregs.void.utility.get

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
data class Animation(
    var first: Int = -1,
    var second: Int = -1,
    var third: Int = -1,
    var fourth: Int = -1,
    var speed: Int = 0
) : Visual {
    override fun reset(character: Character) {
        first = -1
        second = -1
        third = -1
        fourth = -1
        speed = 0
    }
}

const val PLAYER_ANIMATION_MASK = 0x40

const val NPC_ANIMATION_MASK = 0x10

fun Player.flagAnimation() = visuals.flag(PLAYER_ANIMATION_MASK)

fun NPC.flagAnimation() = visuals.flag(NPC_ANIMATION_MASK)

fun Player.getAnimation() = visuals.getOrPut(PLAYER_ANIMATION_MASK) { Animation() }

fun NPC.getAnimation() = visuals.getOrPut(NPC_ANIMATION_MASK) { Animation() }

fun PlayerEvent.animate(id: Int, speed: Int = 0) = player.setAnimation(id, speed)

fun NPCEvent.animate(id: Int, speed: Int = 0) = npc.setAnimation(id, speed)

fun Player.setAnimation(id: Int, speed: Int = 0) {
    setAnimation(getAnimation(), id, speed)
    flagAnimation()
}

fun NPC.setAnimation(id: Int, speed: Int = 0) {
    setAnimation(getAnimation(), id, speed)
    flagAnimation()
}

fun Player.setAnimation(name: String, speed: Int = 0) = setAnimation(get<AnimationDefinitions>().get(name).id, speed)

fun NPC.setAnimation(name: String, speed: Int = 0) = setAnimation(get<AnimationDefinitions>().get(name).id, speed)

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