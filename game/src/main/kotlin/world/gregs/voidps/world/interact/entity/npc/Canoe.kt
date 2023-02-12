package world.gregs.voidps.world.interact.entity.npc

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

suspend fun Interaction.minimumCanoeLevel(): Boolean {
    player<Unsure>("Could you teach me about canoes?")
    if (player.levels.get(Skill.Woodcutting) < 12) {
        npc<Talking>("""
            Well, you don't look like you have the skill to make a
            canoe.
        """)
        npc<Talking>("You need to have at least level 12 woodcutting.")
        npc<Talking>("""
            Once you are able to make a canoe it makes travel
            along the river much quicker!
        """)
        return true
    }
    return false
}