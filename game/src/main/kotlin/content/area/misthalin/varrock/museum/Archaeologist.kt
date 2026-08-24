package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Archaeologist : Script {
    init {
        npcOperate("Talk-to", "marius_giste,barnabus_hurma,caden_azro,thias_leacke,sinco_doar,tinse_torpe") {
//            npc<Happy>("Hello! You're that student who recently finished all three Earth Sciences exams, aren't you? Come to help us out?")
            npc<Happy>("Greetings! Have you come to give us a hand?")
            choice {
                option<Happy>("Yes, how can I help out?") {
                    npc<Sad>("Well, you'll need to get your equipment first - it's all there on the tool rack. Use what you learned from your Earth Sciences exams. You'll need to be wearing your leather gloves and boots as well as have access to")
                    npc<Sad>("your trowel, rock pick and specimen brush.")
                }
                option<Happy>("I found something interesting.") {
                    npc<Happy>("Oh? Let's take a look...")
                    player<Confused>("Err... I seem to have lost it. Sorry.")
                    npc<Laugh>("Sounds like you never had it!")
                }
                option("No thanks.")
            }
        }
    }
}
