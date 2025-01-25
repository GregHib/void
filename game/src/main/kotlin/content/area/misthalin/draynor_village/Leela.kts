package content.area.misthalin.draynor_village

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.Neutral
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "leela") {
    player<Happy>("What are you waiting here for?")
    npc<Neutral>("That is no concern of yours, adventurer.")
}