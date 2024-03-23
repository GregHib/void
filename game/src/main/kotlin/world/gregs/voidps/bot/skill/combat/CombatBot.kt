package world.gregs.voidps.bot.skill.combat

import world.gregs.voidps.bot.Bot
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spellBook

suspend fun Bot.setAttackStyle(skill: Skill) {
    setAttackStyle(when (skill) {
        Skill.Strength -> 1
        Skill.Defence -> 3
        else -> 0
    })
}

suspend fun Bot.setAutoCast(spell: String) {
    val definitions = get<InterfaceDefinitions>()
    val def = definitions.get(player.spellBook)
    player.instructions.emit(InteractInterface(def.id, definitions.getComponentId(player.spellBook, spell) ?: return, -1, -1, 0))
}

suspend fun Bot.setAttackStyle(style: Int) {
    player.instructions.emit(InteractInterface(interfaceId = 884, componentId = style + 11, itemId = -1, itemSlot = -1, option = 0))
}