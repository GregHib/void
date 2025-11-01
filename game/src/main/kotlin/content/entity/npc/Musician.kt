package content.entity.npc

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Musician : Script {

    init {
        npcOperate("Talk-to", "musician*") {
            choice()
        }
    }

    suspend fun Player.choice() {
        choice {
            option<Quiz>("Who are you?") {
                npc<Happy>("Me? I'm a musician Let me help you relax: sit down, rest your weary limbs and allow me to wash away the troubles of the day.")
                npc<Happy>("After a long trek, what could be better than some music to give you the energy to continue?")
                choice()
            }
            option("Can I ask you some questions about resting?") {
                resting()
            }
            exit()
        }
    }

    suspend fun Player.resting() {
        choice("Can I ask you some questions about resting?") {
            option("How does resting work?") {
                player<Quiz>("So how does resting work?")
                npc<Happy>("Have you ever been on a long journey, and simply wanted to have a rest? When you're running from city to city, it's so easy to run out of breath, don't you find?")
                player<Quiz>("Yes, I can never run as far as I'd like.")
                npc<Happy>("Well, you may rest anywhere, simply choose the Rest option on the run buttons.")
                npc<Happy>("When you are nice and relaxed, you will recharge your run energy more quickly and your life points twice as fast as you would do so normally.")
                npc<Talk>("Of course, you can't do anything else while you're resting, other than talk.")
                player<Quiz>("Why not?")
                npc<Happy>("Well, you wouldn't be resting, now would you? Also, you should know that resting by a musician, has a similar effect but the benefits are greater.")
                resting()
            }
            option<Pleased>("What's special about resting by a musician?") {
                npc<Happy>("The effects of resting are enhanced by music. Your run energy will recharge many times the normal rate, and your life points three times as fast.")
                npc<Happy>("Simply sit down and rest as you would normally, nice and close to the musician. You'll turn to face the musician and hear the music. Like resting anywhere, if you do anything other than talk, you will stop resting.")
                resting()
            }
            option<Pleased>("Can you summarise the effects for me?") {
                npc<Happy>("Certainly. You can rest anywhere, simply choose the Rest option on the run buttons.")
                npc<Happy>("Resting anywhere will replenish your run energy more quickly than normal, your life points will replenish twice as fast as well!")
                npc<Happy>("Resting by a musician will replenish your run energy many times faster than normal, and your life points will also replenish three times as fast.")
                resting()
            }
            exit()
        }
    }

    suspend fun ChoiceBuilder2.exit(): Unit = option<Quiz>("That's all for now.") {
        npc<Happy>("Well, don't forget to have a rest every now and again.")
    }
}
