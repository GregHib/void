package content.minigame.sorceress_garden

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class DelMonty : Script {
    init {
        npcOperate("Talk-to", "del_monty") {
            npc<Talk>("Hello, no-fur. What are you doing in my mistress's garden?")
            choice {
                option<Talk>("Looking for sq'irks.") {
                    npc<Talk>("If it's sq'irks you're after then you've come to the right place. We've got four seasons' worth of them.")
                    player<Talk>("I've a couple of questions.")
                    npc<Talk>("I'd be happy to help a friend of the feline.")
                    player<Talk>("What do you mean?")
                    npc<Talk>("The Sphinx gave you that amulet, so she must hold you in high regard.")
                    player<Talk>(" If I remember correctly, it was the High Priest of Sophanem who gave it to me.")
                    npc<Talk>("He and the Sphinx are as thick as thieves, but regardless, I think you've a bit of a cat feel about you.")
                    npc<Talk>("Now what are these questions?")
                    questions()
                }
                option<Talk>("Talking to cats.") {
                    npc<Talk>("A noble and rewarding past-time.")
                    choice {
                        option<Quiz>("How did you get here?") {
                            getHere()
                        }
                        option<Talk>("What are you doing here?") {
                            doingHere()
                        }
                        option<Quiz>("Who are you?") {
                            whoAreYou()
                        }
                    }
                }
                option<Talk>("Nothing much.") {
                    npc<Talk>("Yawn! Nice talking to you then, no-fur.")
                }
            }
        }
    }

    suspend fun Player.questions() {
        choice {
            option<Talk>("What are the creatures inside the gardens?") {
                npc<Talk>("Oh, you mean the gardeners?")
                player<Uncertain>("The strange, floaty creatures.")
                npc<Talk>("Yes, the gardeners. Don't let them see you or they'll teleport you out here. They're very protective of their crops.")
                moreQuestions()
            }
            option<Talk>("How do you get into the seasonal gardens?") {
                npc<Talk>("I get in through gaps in the hedge. You are a little too big to squeeze through. I think you'll have to use the gates.")
                npc<Talk>("I think the gates are locked, so you'll have to have good Thieving skills to get in.")
                moreQuestions()
            }
            option<Talk>("How is it that the gardens are in different seasons?") {
                npc<Talk>("How did you get here?")
                player<Uncertain>("Magic?")
                npc<Talk>("Exactly. The Sorceress likes to have a good supply of in-season sq'irks and herbs at all times.")
                moreQuestions()
            }
            option<Talk>("How do I get out of here?") {
                npc<Talk>("Take a drink from the fountain.")
                moreQuestions()
            }
        }
    }

    suspend fun Player.moreQuestions() {
        choice {
            option<Talk>("Thanks, I have another question though.") {
                questions()
            }
            option<Talk>("Thanks for your help.")
        }
    }

    suspend fun Player.getHere() {
        npc<Talk>("Every time I play with spiders in the Sorceress's house, her silly apprentice completely freaks out and teleports me here!")
        player<Talk>("So you've been stuck here since?")
        npc<Talk>("No, silly! I drink from the fountain whenever I want to leave.")
        anotherQuestion()
    }

    suspend fun Player.doingHere() {
        npc<Talk>("I get this strange urge for sq'irks. It's quite peculiar. I think I may be addicted.")
        player<Talk>("I think I know someone else who may be in a similar position.")
        npc<Talk>("Don't tell me. Osman, right?")
        player<Uncertain>("You know Osman?")
        npc<Talk>("Oh, he used to come here all the time. Then one day, he just stopped.")
        anotherQuestion()
    }

    suspend fun Player.whoAreYou() {
        npc<Talk>("Del-Monty the cat, at your service.")
        player<Talk>("Are you a famous adventurer who was turned into a cat by a vindictive mage?")
        npc<Talk>("No; as I said, I'm Del-Monty the cat, connoisseur of exotic fruits.")
        player<Talk>("In that case, can you tell me anything about sq'irks?")
        npc<Talk>("But of course! Their juice is an excellent source of energy for runners, and in the riper varieties they are known to heighten one's Thieving abilities.")
        anotherQuestion()
    }

    suspend fun Player.anotherQuestion() {
        choice {
            option<Talk>("Thanks, I have another question though.") {
                choice {
                    option<Quiz>("How did you get here?") {
                        getHere()
                    }
                    option<Talk>("What are you doing here?") {
                        doingHere()
                    }
                    option<Quiz>("Who are you?") {
                        whoAreYou()
                    }
                }
            }
            option<Talk>("Thanks for your help.")
        }
    }
}
