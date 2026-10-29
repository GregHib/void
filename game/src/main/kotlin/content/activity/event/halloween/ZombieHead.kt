package content.activity.event.halloween

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Cry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import kotlinx.coroutines.delay
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.TICKS
import kotlin.time.Duration.Companion.milliseconds

class ZombieHead : Script {

    init {
        itemOption("Talk-to", "zombie_head") {
            talkAt()
        }

        itemOption("Talk-At", "zombie_head") {
            talkAt()
        }

        itemOption("Display", "zombie_head") {
            Script.launch {
                performEmote("emote_zombie_head_display")
            }
            say("Mwuhahahaha!")
        }

        itemOption("Question", "zombie_head") {
            zombieHeadQuestions()
        }
    }

    private fun Player.talkAt() {
        Script.launch {
            performEmote("emote_zombie_head_talk")
        }
        say("Alas!")
    }

    private suspend fun Player.performEmote(animation: String) {
        val cooldown = "zombie_head_emote_until_$animation"
        if (GameLoop.tick < get(cooldown, 0)) {
            return
        }
        val ticks = anim(animation)
        set(cooldown, GameLoop.tick + ticks.coerceAtLeast(1))
        delay(TICKS.toMillis(ticks).milliseconds)
    }

    private suspend fun Player.zombieHeadQuestions() {
        choice {
            option("How did you die?") {
                player<Quiz>("Hey, Head?")
                npc<Quiz>("zombie_head", "What?")
                player<Neutral>("How did you die?")
                npc<Sad>("zombie_head", "I stuck my neck out for an old friend.")
                player<Neutral>("You shouldn't get so cut up about it.")
                npc<Sad>("zombie_head", "Well if I keep it all bottled up I'll turn into a total head case.")
            }
            option("What is your name?") {
                player<Quiz>("Hey, Head?")
                npc<Quiz>("zombie_head", "What?")
                player<Quiz>("What is your name?")
                npc<Shifty>("zombie_head", "Mumblemumblemumble...")
                player<Quiz>("What was that?")
                npc<Sad>("zombie_head", "My name is Edward Cranium.")
                player<Laugh>("Edd Cranium?")
                player<Laugh>("Hahahahahahahahahahaha!")
                npc<Sad>("zombie_head", "Har har...")
            }
            option("Can you do any tricks?") {
                player<Quiz>("Hey Head?")
                npc<Quiz>("zombie_head", "What now?")
                player<Quiz>("Can you do any tricks?")
                npc<Sad>("zombie_head", "Not any more...")
                player<Quiz>("How come?")
                npc<Sad>("zombie_head", "Because I used to be able to do a handstand for over an hour while juggling cannon balls with my feet...")
                player<Neutral>("Wow, you were quite the entertainer.")
                npc<Sad>("zombie_head", "Yep. Now I can barely roll my eyes...")
                player<Happy>("I know what you can do!")
                npc<Quiz>("zombie_head", "What?")
                player<Happy>("Vent...")
                npc<Angry>("zombie_head", "Don't even suggest it!")
                player<Sad>("Ok.")
            }
            option("Want a new hat?") {
                player<Quiz>("Hey, Head?")
                npc<Sad>("zombie_head", "Can't I rest in peace?")
                player<Happy>("No!")
                player<Quiz>("Would you like a new hat?")
                npc<Shifty>("zombie_head", "No, but could you screw a handle into the top of my head?")
                player<Quiz>("A handle? Why?")
                npc<Neutral>("zombie_head", "Because currently you wave me about by my hair, and it hurts!")
            }
            option("Want to go scare some people?") {
                player<Quiz>("Hey, Head?")
                npc<Angry>("zombie_head", "Will you ever leave me alone?")
                player<Happy>("No!")
                player<Quiz>("Want to go scare some people?")
                npc<Sad>("zombie_head", "Let's leave it for now.")
                player<Sad>("All right...")
                player<Happy>("We'll quit while we're ahead!")
                npc<Cry>("zombie_head", "If I try really hard I might be able to will myself deader...")
            }
        }
    }
}
