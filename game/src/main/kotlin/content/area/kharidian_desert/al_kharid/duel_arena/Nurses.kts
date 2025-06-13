package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate

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
