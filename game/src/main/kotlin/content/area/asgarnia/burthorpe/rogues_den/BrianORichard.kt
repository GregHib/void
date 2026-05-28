package content.area.asgarnia.burthorpe.rogues_den

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class BrianORichard : Script {
    init {
        npcOperate("Talk-to", "brian_o_richard") {
            npc<Happy>("Hi there, looking for a challenge are you?")
            choice {
                whatHaveYouGot()
                thisPlace()
                option<Neutral>("No thanks.")
            }
        }
    }

    private fun ChoiceOption.whatHaveYouGot() {
        option<Quiz>("Yes actually, what've you got?") {
            npc<Happy>("Aha, I have the perfect thing for you! See if you can get to the centre of my maze, the further you get the greater the rewards. There's even some special prizes if you make it right to the end.")
            choice {
                option<Happy>("Ok that sounds good!") {
                    npc<Happy>("Great! When you enter the maze, I'll give you a jewel - it'll allow you to get out of the maze at any time. However that's all you're allowed to take in with you, no cheating!")
                    npc<Shifty>("Oh one last thing, if you happen to see my harmonica I'd really like to have it back.")
                }
                thisPlace()
                option<Neutral>("Actually I think I'll pass thanks.")
            }
        }
    }

    private fun ChoiceOption.thisPlace() {
        option<Quiz>("What is this place?") {
            npc<Happy>("Ah welcome to my humble home, well actually it belongs to mummsie but she's getting on a bit so I look after the place for her.")
            npc<Happy>("So are you interested in a challenge?")
            choice {
                whatHaveYouGot()
                option<Neutral>("No thanks.")
            }
        }
    }
}
