package content.area.asgarnia.falador

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class KarmaTheChameleon : Script {

    init {
        npcOperate("Talk-to", "karma_the_chameleon") {
            if (levels.get(Skill.Summoning) < 100) {
                npc<Neutral>("Hsssssss ssssshssssss hssss hsss hssss!")
                return@npcOperate
            }
            npc<Neutral>("Hssshsss hssssh sssshssss sss.<br>(Party on, dude!)")
            choice {
                option<Confused>("Who are you?") {
                    npc<Neutral>("Hssshsss hssssh sssshssss sss.<br>(My name's Karma. I'm Pete's pet.)")
                    player<Neutral>("That's nice.")
                    npc<Neutral>("Hssshsss hssssh sssshssss sss.<br>(Oh yeah, man, it majorly rocks!)")
                    player<Happy>("Rock on!")
                    npc<Neutral>("Hssshsss hssssh sssshssss sss.<br>(You betcha!)")
                }
                option<Happy>("Rock on!") {
                    npc<Neutral>("Hssshsss hssssh sssshssss sss.<br>(You betcha!)")
                }
            }
        }
    }
}

// todo make karma follow party pete and change to a random colour every 10-16 seconds
