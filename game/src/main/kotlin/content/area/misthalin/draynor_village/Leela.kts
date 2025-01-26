package content.area.misthalin.draynor_village

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player

npcOperate("Talk-to", "leela") {
    player<Happy>("What are you waiting here for?")
    npc<Neutral>("That is no concern of yours, adventurer.")
}