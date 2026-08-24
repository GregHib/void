package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class SchoolPupil : Script {
    init {
        npcOperate("Talk-to", "schoolboy*,schoolgirl*") {
            when (random.nextInt(7)) {
                0 -> npc<Happy>("Maz...Zar...Za-mor-ak is bestest!")
                1 -> npc<Happy>("*sniff* They won't let me take an arrowhead as a souvenir.")
                2 -> npc<Happy>("Can you find my teacher? I need the toilet!")
                3 -> npc<Happy>("I wanna be an archaeologist when I grow up!")
                4 -> npc<Happy>("Sada...Sram...Sa-ra-do-min is bestest!")
                5 -> npc<Happy>("Teacher! Can we go to the Natural History exhibit now?")
                6 -> npc<Happy>("Yaaay! A day off school.")
                7 -> npc<Happy>("The Kalphite Queen is soo scary!")
                8 -> npc<Happy>("I bet a wyvern would beat a dragon in a fight!")
                9 -> npc<Happy>("We're going to see the dragons! They're my favourite!")
                10 -> npc<Happy>("My dad says he got bitten by a giant snail once.")
                11 -> npc<Happy>("Do you know where the moles are?")
            }
        }
    }
}
