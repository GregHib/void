package content.area.misthalin.barbarian_village

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random

class Kjell : Script {

    init {
        npcOperate("Talk-to", "kjell_*") {
            when (quest("gunnars_ground")) {
                "gunnars_ground", "completed" -> completed()
                "started" -> {
                }
                else -> unstarted()
            }
        }
    }

    suspend fun Player.completed() {
        npc<Neutral>(
            when (random.nextInt(0, 9)) {
                0 -> "...there's a place for us..."
                1 -> "...but I'd do anything for you..."
                2 -> "...you exploded into my heart..."
                3 -> "...love you like the stars above..."
                4 -> "...I dreamed your dream for you..."
                5 -> "...there's a place for us..."
                6 -> "...fall for chains of gold..."
                7 -> "...when you gonna realise..."
                else -> "...fall for pretty strangers..."
            },
        )
        npc<Angry>("Blast!")
        choice {
            option<Idle>("Having trouble there?") {
                npc<Angry>("I don't need the advice of an outerlander.")
                advice()
            }
            option<Idle>("I'll leave you in peace.") {
            }
        }
    }

    suspend fun Player.advice() {
        choice {
            option<Idle>("This music isn't very restful.") {
                npc<Angry>("Get out of here!")
            }
            option<Idle>("Maybe you should take some lessons.") {
                npc<Angry>("Get out of here!")
            }
            option<Idle>("I'll leave you in peace.") {
                npc<Angry>("Get out of here!")
            }
        }
    }

    suspend fun Player.unstarted() {
        npc<Frustrated>("Get out of here, outerlander!")
        choice {
            option<Idle>("What is this place?") {
                npc<Frustrated>("The barbarian village. Go away.")
            }
            option<Idle>("Who are you?") {
                npc<Frustrated>("My name is Kjell. Go away.")
            }
            option<Idle>("What's in this hut you're guarding?") {
                npc<Frustrated>("Nothing yet. Once there is, no one will get in or out! Now, Go away!")
            }
            option<Idle>("Goodbye then.") {
            }
        }
    }
}
