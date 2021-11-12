package world.gregs.voidps.bot.skill.combat

import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.instruct.InteractInterface

suspend fun Bot.setAttackStyle(skill: Skill) {
    setAttackStyle(when (skill) {
        Skill.Strength -> 1
        Skill.Defence -> 3
        else -> 0
    })
}

suspend fun Bot.setAttackStyle(style: Int) {
    player.instructions.emit(InteractInterface(interfaceId = 884, componentId = style + 11, itemId = -1, itemSlot = -1, option = 0))
}