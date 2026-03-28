package content.area.misthalin.draynor_village.manor

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele

class ProfessorOddenstein : Script {
    init {
        npcOperate("Talk-to", "professor_oddenstein") { (target) ->
            if (!questCompleted("ernest_the_chicken")) {
                // TODO quest
                npc<Neutral>("Be careful in here, there's lots of dangerous equipment.")
                choice {
                    option<Quiz>("What does this machine do?") {
                        npc<Neutral>("Nothing at the moment... It's broken. It's meant to be a transmutation machine.")
                        npc<Neutral>("It has also spent time as a time travel machine, and a dramatic lightning generator, and a thing for generating monsters.")
                    }
                    option<Quiz>("Is this your house?") {
                        npc<Neutral>("No, I'm just one of the tenants. It belongs to the count who lives in the basement.")
                    }
                }
                return@npcOperate
            }
            // TODO on enter portal without boots
            npc<Confused>("Errr, just before you go through there...")
            player<Quiz>("What's the problem?")
            npc<Neutral>("That portal opens into a plane populated with some very shocking creatures. You should wear some kind of insulated armour before going there.")
            choice {
                option<Quiz>("Where can I get insulated armour from?") {
                    npc<Neutral>("Well there were some pretty tough people here last week. Said they were Slayer Masters. They were planning on making some protective boots. You should speak to one of them.")
                    choice {
                        option<Neutral>("Thanks, I'll do that.") {
                            npc<Neutral>("No problem. See you later.")
                        }
                        option("I don't want to run around after Slayer Masters, I'm going through.") {
                            tele(2677, 5214, 2)
                        }
                    }
                }
                option<Neutral>("Thanks, I think I'll stay here for a while then.")
                option("Thanks for the warning, but I'm not scared of any monster.") {
                    player<Neutral>("Thanks for the warning, but I'm not scared of any monster.")
                    npc<Neutral>("Ok. Just don't say I didn't warn you")
                    tele(2677, 5214, 2)
                }
            }
        }
    }
}
