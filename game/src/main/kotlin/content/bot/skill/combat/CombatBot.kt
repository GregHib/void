package content.bot.skill.combat

import content.bot.Bot
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.client.instruction.InteractInterface
import content.skill.magic.spell.spellBook

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
    player.instructions.send(InteractInterface(def.id, definitions.getComponentId(player.spellBook, spell) ?: return, -1, -1, 0))
}

suspend fun Bot.setAttackStyle(style: Int) {
    player.instructions.send(InteractInterface(interfaceId = 884, componentId = style + 11, itemId = -1, itemSlot = -1, option = 0))
}