package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class GiantEnt : Script {
    init {
        npcOperate("Interact", "giant_ent_familiar") {
            val stage = this["ent_fam_dial", 0]
            if (stage == 0) {
                this["ent_fam_dial"] = 1
                npc<Neutral>("Creeeeeeeeeeeak..... (I.....)")
                player<Happy>("Yes?")
                npc<Neutral>(".....")
                statement("After a while you realise that the ent has finished speaking for the moment.")
                return@npcOperate
            }
            if (stage == 1) {
                this["ent_fam_dial"] = 2
                npc<Neutral>("Creak..... Creaaaaaaaaak..... (Am.....)")
                player<Happy>("Yes?")
                npc<Neutral>(".....")
                statement("After a while you realise that the ent has finished speaking for the moment.")
                return@npcOperate
            }
            if (stage == 2) {
                this["ent_fam_dial"] = 3
                npc<Neutral>("Grooooooooan..... (Feeling.....)")
                player<Happy>("Yes? We almost have a full sentence now - the suspense is killing me!")
                npc<Neutral>(".....")
                statement("After a while you realise that the ent has finished speaking for the moment.")
                return@npcOperate
            }
            this["ent_fam_dial"] = 0
            when (random.nextInt(5)) {
                0 -> {
                    npc<Neutral>("Groooooooooan..... (Sleepy.....)")
                    player<Happy>("I'm not sure if that was worth all the waiting.")
                }
                1 -> {
                    npc<Neutral>("Grooooooan.....creeeeeeeak (Restful.....)")
                    player<Happy>("I'm not sure if that was worth all the waiting.")
                }
                2 -> {
                    npc<Neutral>("Grrrrooooooooooooooan..... (Achey.....)")
                    player<Happy>("I'm not sure if that was worth all the waiting.")
                }
                3 -> {
                    npc<Neutral>("Creeeeeeeegroooooooan..... (Goood.....)")
                    player<Happy>("I'm not sure if that was worth all the waiting.")
                }
                4 -> {
                    npc<Neutral>("Creeeeeeeeeeeeeaaaaaak..... (Tired.....)")
                    player<Happy>("I'm not sure if that was worth all the waiting.")
                }
            }
        }
    }
}
