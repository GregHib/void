package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Angry
import world.gregs.voidps.world.interact.dialogue.Laugh
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "jaraah" && option == "Talk-to" }) { player: Player ->
    player("cheerful", "Hi!")
    npc<Angry>("What? Can't you see I'm busy?!")
    val choice = choice("""
        Can you heal me?
        You must see some gruesome things?
        Why do they call you 'The Butcher'?
    """)
    when (choice) {
        1 -> {
            player("uncertain", "Can you heal me?")
            heal()
        }
        2 -> {
            player("uncertain", "You must see some gruesome things?")
            npc<Angry>("""
                It's a gruesome business and with the tools they give
                me it gets more gruesome before it gets better!
            """)
            player("laugh", "Really?")
            npc<Laugh>("It beats being stuck in the monastery!")
        }
        3 -> {
            player("uncertain", "Why do they call you 'The Butcher'?")
            npc<Laugh>("'The Butcher'?")
            npc<Angry>("Ha!")
            npc<Angry>("Would you like me to demonstrate?")
            player("surprised", "Er...I'll give it a miss, thanks.")
        }
    }
}

on<NPCOption>({ npc.id == "jaraah" && option == "Heal" }) { player: Player ->
    heal()
}