package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.move.retreat
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.forceChat
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "hans" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("talk", "Hello. What are you doing here?")
        val choice = choice("""
            I'm looking for whoever is in charge of this place.
            I have come to kill everyone in this castle!
            I don't know. I'm lost. Where am I?
        """)
        when (choice) {
            1 -> {
                player("talk", "I'm looking for whoever is in charge of this place.")
                npc("talk", "Who, the Duke? He's in his study, on the first floor.")
            }
            2 -> {
                player("evil_laugh", "I'm looking for whoever is in charge of this place.")
                npc.forceChat = "Help! Help!"
                npc.retreat(player)
            }
            3 -> {
                player("uncertain", "I don't know. I'm lost. Where am I?")
                npc("talk", "You are in Lumbridge Castle.")
            }
        }
    }
}