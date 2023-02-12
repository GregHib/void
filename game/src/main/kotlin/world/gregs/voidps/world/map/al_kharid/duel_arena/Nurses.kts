package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ (npc.id == "sabreen" || npc.id == "a_abla") && option == "Talk-to" }) { player: Player ->
    player<Cheerful>("Hi!")
    npc<Cheerful>("Hi. How can I help?")
    val choice = choice("""
        Can you heal me?
        Do you see a lot of injured fighters?
        Do you come here often?
    """)
    when (choice) {
        1 -> {
            player<Uncertain>("Can you heal me?")
            heal()
        }
        2 -> fighters()
        3 -> often()
    }
}

on<NPCOption>({ (npc.id == "sabreen" || npc.id == "a_abla") && option == "Heal" }) { player: Player ->
    heal()
}