package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.event.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.dialogue.Neutral
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

suspend fun CharacterContext<Player>.minimumCanoeLevel(): Boolean {
    player<Quiz>("Could you teach me about canoes?")
    if (player.levels.get(Skill.Woodcutting) < 12) {
        npc<Neutral>("Well, you don't look like you have the skill to make a canoe.")
        npc<Neutral>("You need to have at least level 12 woodcutting.")
        npc<Neutral>("Once you are able to make a canoe it makes travel along the river much quicker!")
        return true
    }
    return false
}