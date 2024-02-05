package world.gregs.voidps.world.map.draynor

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate({ target.id == "leela" && option == "Talk-to" }) { player: Player ->
    player<Cheerful>("What are you waiting here for?")
    npc<Talking>("That is no concern of yours, adventurer.")
}