package world.gregs.voidps.bot.skill.combat

import world.gregs.voidps.bot.Bot
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.getComponentIntId
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.instruct.InteractInterface
import world.gregs.voidps.world.interact.entity.combat.spellBook

suspend fun Bot.setAttackStyle(skill: Skill) {
    setAttackStyle(when (skill) {
        Skill.Strength -> 1
        Skill.Defence -> 3
        else -> 0
    })
}

suspend fun Bot.setAutoCast(spell: String) {
    val def = get<InterfaceDefinitions>().get(player.spellBook)
    player.instructions.emit(InteractInterface(def.actualId, def.getComponentIntId(spell) ?: return, -1, -1, 0))
}

suspend fun Bot.setAttackStyle(style: Int) {
    player.instructions.emit(InteractInterface(interfaceId = 884, componentId = style + 11, itemId = -1, itemSlot = -1, option = 0))
}