package world.gregs.voidps.world.map.draynor

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "leela") {
    player<Cheerful>("What are you waiting here for?")
    npc<Talking>("That is no concern of yours, adventurer.")
}