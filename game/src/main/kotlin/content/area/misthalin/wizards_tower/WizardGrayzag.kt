package content.area.misthalin.wizards_tower

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name

class WizardGrayzag : Script {
    init {
        npcOperate("Talk-to", "wizard_grayzag") {
            when (quest("imp_catcher")) {
                "started" -> npc<Laugh>("You're a fool, $name. Do you really think you'll find four imps out of thousands? Good luck. Ha!")
                "completed", "given_beads" -> {
                    npc<Angry>("So you think finding those beads makes you clever, do you?")
                    choice {
                        option<Happy>("Well yes, actually.") {
                            npc<Angry>("Well you'd better just watch your back, because when you least expect it I'll be there. You shouldn't go sticking your nose into other people's affairs, meddler.")
                        }
                        demon()
                        option<Neutral>("Never mind.")
                    }
                }
                else -> {
                    npc<Angry>("Not now.<br>I'm trying to concentrate on a very difficult spell!")
                    choice {
                        demon()
                        option<Neutral>("Oh, sorry.")
                    }
                }
            }
        }
    }

    private fun ChoiceOption.demon() {
        option<Quiz>("Is that your demon?") {
            npc<Angry>("Did I summon it, do you mean? Certainly not, although I am the only wizard in this Tower with the expertise to guard and study it.")
            choice {
                option<Quiz>("How did it get here, then?") {
                    npc<Angry>("It's been here longer than anyone remembers. I daresay some foolish wizard summoned it while meddling with forces they did not fully understand.")
                    npc<Neutral>("Every time it's destroyed it reappears on the same spot, so we built a containment ward around it and left it there.")
                }
                option<Neutral>("Oh, sorry.")
            }
        }
    }
}
