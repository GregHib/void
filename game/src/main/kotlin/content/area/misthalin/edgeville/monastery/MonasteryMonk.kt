package content.area.misthalin.edgeville.monastery

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class MonasteryMonk : Script {
    init {
        npcOperate("Talk-to", "monk_edgeville") { (target) ->
            npc<Neutral>("Greetings traveller.")
            choice {
                option<Quiz>("Can you heal me? I'm injured.") {
                    npc<Neutral>("Ok.")
                    message("You feel a little better.")
                    gfx("heal")
                    areaSound("heal", tile, radius = 10)
                    levels.restore(Skill.Constitution, levels.getOffset(Skill.Constitution))
                }
                option<Quiz>("Isn't this place built a bit out of the way?") {
                    npc<Neutral>("We like it that way actually! We get disturbed less. We still get rather a large amount of travellers looking for sanctuary and healing here as it is!")
                }
                if (!get("edgeville_monastery_order_member", false)) {
                    option<Quiz>("How do I get further into the monastery?") {
                        npc<Neutral>("You'll need to talk to Abbot Langley about that. He's usually to be found walking the halls of the monastery.")
                    }
                }
            }
        }
    }
}
