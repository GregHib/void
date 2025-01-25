package content.area.kharidian_desert.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "sabreen", "a_abla") {
    player<Happy>("Hi!")
    npc<Happy>("Hi. How can I help?")
    choice {
        option<Uncertain>("Can you heal me?") {
            heal()
        }
        fighters()
        often()
    }
}

npcOperate("Heal", "sabreen", "a_abla") {
    heal()
}