package world.gregs.voidps.world.map.random_events

import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.npc

on<NPCOption>({ npc.id == "quiz_master" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("sad", "Sorry, it's usernamehere that's having the lucky day.")
    }
}
