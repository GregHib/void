package content.area.misthalin.varrock

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class CookJollyBoarInn : Script {
    init {
        npcSpawn("cook_jolly_boar_inn") {
            softTimers.start("cook_jolly_boar_inn_cook")
        }

        npcTimerStart("cook_jolly_boar_inn_cook") {
            TimeUnit.SECONDS.toTicks(7)
        }

        npcTimerTick("cook_jolly_boar_inn_cook") {
            anim("896")
            Timer.CONTINUE
        }

        npcOperate("Talk-to", "cook_jolly_boar_inn") {
            if (hasClock(COOLDOWN)) {
                repeatDialogue()
            } else {
                start(COOLDOWN, 100)
                standardDialogue()
            }
        }
    }

    private suspend fun Player.standardDialogue() {
        npc<Angry>("What do you want? I'm busy!")
        cookOptions()
    }

    private suspend fun Player.repeatDialogue() {
        start(COOLDOWN, 100)
        player<Neutral>("Hello...")
        npc<Angry>("I distinctly remember you saying you were going to stop talking to me. Yet here you are again, yakking on and on in my earhole when I'm trying to cook! What do you want now?")
        cookOptions()
    }

    private suspend fun Player.cookOptions() {
        choice {
            option<Neutral>("Can you cook me something?") {
                npc<Angry>("What? No! Go away.")
                player<Quiz>("Well, you're meant to be a cook.<br>Why can't you cook something for me?")
                npc<Angry>("I don't want to, that's why!<br>Now, leave me alone.")
                player<Confused>("You're not a very nice man.")
                npc<Angry>("Yet you keep talking to me!")
                player<Confused>("Good point. I should stop that.")
                npc<Angry>("Yes, do us both a favour.")
            }
            option<Quiz>("Why do you work here?") {
                npc<Angry>("Why do you think I work in a disreputable inn filled with rough scoundrels on the very edge of the Wilderness? I work here because I need the job!")
                player<Confused>("Do you like it?")
                npc<Sad>("Like it? It's sucked the warmth and humanity out of me, and I've become a hollow shell holding nothing but anger, misery and gourmet recipes.")
                player<Sad>("Oh.")
                npc<Shifty>("What did you expect to hear? I suppose you expected me to tell you an inspiring story about how working here can teach you to find hope in the darkest places, or how to see the best in everything?")
                npc<Angry>("Well, get those fancy ideas out of your head. Out here you've got nothing but your armour and muscle to protect you from the endless dark and the screaming hordes coming to kill you!")
                player<Quiz>("Maybe you should think about<br>moving to a different inn.")
                npc<Angry>("Bah! I'm not letting it defeat me.<br>Now, let me concentrate on my cooking.")
                player<Neutral>("Okay.")
            }
            option<Quiz>("Can you tell me any good jokes?") {
                npc<Angry>("Ohhh, if you insist...")
                npc<Bored>("What did the half-wit say to the cook?")
                player<Unamused>("I don't know.")
                npc<Happy>("${if (male) "He" else "She"} said 'Can you tell me any good jokes?'<br>Got ya! Hahahaha!")
                player<Bored>("Whatever.")
            }
        }
    }

    private companion object {
        const val COOLDOWN = "cook_jolly_boar_inn_talk_cooldown"
    }
}
