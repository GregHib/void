package world.gregs.voidps.world.activity.skill.slayer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

val Character.isUndead: Boolean
    get() = false

val Player.hasSlayerTask: Boolean
    get() = false

fun Player.isTask(character: Character?): Boolean {
    return false
}