package content.area.misthalin.varrock

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.Timer

class CaptainVarrock : Script {

    private val overheads = listOf(
        "Make those dummies suffer!",
        "Imagine it's your aunt!",
        "You won't last 2 minutes in the real world!",
        "Show some backbone!",
        "Let's get some proper bloodlust here!",
        "Bah, why do I bother with you lot?",
        "Just hit the thing already!",
        "Hit the dummy! Hit the dummy!",
        "Useless! Utterly useless!",
        "Hit the dummy before I come over there and hit you!",
    )

    init {
        npcSpawn("captain_varrock") {
            softTimers.start("captain_varrock_overhead")
        }
        npcTimerStart("captain_varrock_overhead") {
            20
        }
        npcTimerTick("captain_varrock_overhead") {
            say(overheads.random())
            Timer.CONTINUE
        }
        npcOperate("Talk-to", "captain_varrock") {
            standardDialogue()
        }
    }

    private suspend fun Player.standardDialogue() {
        npc<Angry>("What do you want, civilian?")
        choice("What would you like to say?") {
            option("What are you doing?") {
                player<Quiz>("What are you doing?")
                npc<Angry>("What does it look like I'm doing?<br>I'm training this useless bunch of recruits!")
                recruitsDialogue()
            }
            option("Can I have some armour like yours?") {
                armourDialogue()
            }
            option("I want nothing.") {
                player<Happy>("I want nothing.")
                npc<Angry>("Lucky you. Go away.")
            }
        }
    }

    private suspend fun Player.recruitsDialogue(previousOpinion: RecruitOpinion? = null) {
        choice("What would you like to say?") {
            when (previousOpinion) {
                RecruitOpinion.FIT -> {
                    option("Can I have some armour like yours?") {
                        armourDialogue()
                    }
                    option("Oh yes, they look very weak.") {
                        player<Neutral>("Oh yes, they look very weak.")
                        npc<Angry>("Have you lost your mind?<br>Earlier you said they looked fit!<br>Just go away and leave me alone.")
                    }
                }
                RecruitOpinion.WEAK -> {
                    option("Can I have some armour like yours?") {
                        armourDialogue()
                    }
                    option("Oh yes, they look very fit.") {
                        player<Neutral>("Oh yes, they look very fit.")
                        npc<Angry>("Have you lost your mind?<br>Earlier you said they looked weak!<br>Just go away and leave me alone.")
                    }
                }
                null -> {
                    option("Oh yes, they look very fit.") {
                        player<Happy>("Oh yes, they look very fit.")
                        npc<Angry>("They're nothing but a useless bunch of weaklings who won't survive an hour once they're out on patrol.")
                        recruitsDialogue(RecruitOpinion.FIT)
                    }
                    option("Oh yes, they look very weak.") {
                        player<Happy>("Oh yes, they look very weak.")
                        npc<Angry>("Show some respect when you talk about my men, civilian!")
                        recruitsDialogue(RecruitOpinion.WEAK)
                    }
                    option("Can I have some armour like yours?") {
                        armourDialogue()
                    }
                }
            }
        }
    }

    private suspend fun Player.armourDialogue() {
        player<Happy>("Can I have some armour like yours?")
        npc<Angry>("Only guards of Varrock can wear this armour.")
        player<Happy>("Well, I could become a guard of Varrock.")
        if (levels.get(Skill.Thieving) >= 31) {
            npc<Angry>("Oh yeah? I see you've got a Thieving level of ${levels.get(Skill.Thieving)}. That must give you quite a long criminal record.")
            player<Shifty>("I could repent of my evil ways if you like.")
        }
        npc<Shifty>("So you really want to be a guard, huh?")
        player<Happy>("Yes, that's right.")
        npc<Shifty>("Does it worry you that the life expectancy of a guard is about 30 seconds?")
        choice("What would you like to say?") {
            option("No, that doesn't worry me.") {
                player<Neutral>("No, that doesn't worry me.")
                npc<Laugh>("Bah! You just failed the intelligence test. Go away, civilian.")
            }
            option("Actually, forget it.") {
                player<Bored>("Actually, forget it.")
                npc<Laugh>("Go away, coward.")
            }
        }
    }

    private enum class RecruitOpinion {
        FIT,
        WEAK,
    }
}
