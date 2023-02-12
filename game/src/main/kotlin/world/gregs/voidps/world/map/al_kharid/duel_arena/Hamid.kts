package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "hamid" && option == "Talk-to" }) { player: Player ->
    player("cheerful", "Hi!")
    npc<Talking>("Hello traveller. How can I be of assistance?")
    val choice = choice("""
        Can you heal me?
        What's a Monk doing in a place such as this?
        Which monastery do you come from?
    """)
    when (choice) {
        1 -> {
            player("uncertain", "Can you heal me?")
            npc<Talking>("You'd be better off speaking to one of the nurses.")
            npc<Cheerful>("They are so... nice... afterall!")
        }
        2 -> {
            player("uncertain", "What's a Monk doing in a place such as this?")
            npc<Suspicious>("""
                Well don't tell anyone but I came here because of the
                nurses!
            """)
            player("laugh", "Really?")
            npc<Laugh>("It beats being stuck in the monastery!")
        }
        3 -> {
            player("uncertain", "Which monastery do you come from?")
            npc<Talking>("I belong to the monastery north of Falador.")
            player("uncertain", "You're a long way from home?")
            npc<Sad>("Yeh. I miss the guys.")
        }
    }
}
