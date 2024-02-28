package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "jaraah") {
    player<Cheerful>("Hi!")
    npc<Angry>("What? Can't you see I'm busy?!")
    choice {
        option<Uncertain>("Can you heal me?") {
            heal()
        }
        option<Uncertain>("You must see some gruesome things?") {
            npc<Angry>("It's a gruesome business and with the tools they give me it gets more gruesome before it gets better!")
            player<Chuckle>("Really?")
            npc<Chuckle>("It beats being stuck in the monastery!")
        }
        option<Uncertain>("Why do they call you 'The Butcher'?") {
            npc<Chuckle>("'The Butcher'?")
            npc<Angry>("Ha!")
            npc<Angry>("Would you like me to demonstrate?")
            player<Surprised>("Er...I'll give it a miss, thanks.")
        }
    }
}

npcOperate("Heal", "jaraah") {
    heal()
}
