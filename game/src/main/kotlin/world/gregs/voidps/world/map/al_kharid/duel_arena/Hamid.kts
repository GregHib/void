package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "hamid") {
    player<Happy>("Hi!")
    npc<Neutral>("Hello traveller. How can I be of assistance?")
    choice {
        option<Uncertain>("Can you heal me?") {
            npc<Neutral>("You'd be better off speaking to one of the nurses.")
            npc<Happy>("They are so... nice... afterall!")
        }
        option<Uncertain>("What's a Monk doing in a place such as this?") {
            npc<Shifty>("Well don't tell anyone but I came here because of the nurses!")
            player<Chuckle>("Really?")
            npc<Chuckle>("It beats being stuck in the monastery!")
        }
        option<Uncertain>("Which monastery do you come from?") {
            npc<Neutral>("I belong to the monastery north of Falador.")
            player<Uncertain>("You're a long way from home?")
            npc<Sad>("Yeh. I miss the guys.")
        }
    }
}
