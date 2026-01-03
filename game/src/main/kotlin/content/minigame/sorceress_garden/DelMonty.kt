package content.minigame.sorceress_garden

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class DelMonty : Script {
    init {
        npcOperate("Talk-to", "del_monty") {
            npc<Neutral>("Hello, no-fur. What are you doing in my mistress's garden?")
            choice {
                option<Neutral>("Looking for sq'irks.") {
                    npc<Neutral>("If it's sq'irks you're after then you've come to the right place. We've got four seasons' worth of them.")
                    player<Neutral>("I've a couple of questions.")
                    npc<Neutral>("I'd be happy to help a friend of the feline.")
                    player<Neutral>("What do you mean?")
                    npc<Neutral>("The Sphinx gave you that amulet, so she must hold you in high regard.")
                    player<Neutral>(" If I remember correctly, it was the High Priest of Sophanem who gave it to me.")
                    npc<Neutral>("He and the Sphinx are as thick as thieves, but regardless, I think you've a bit of a cat feel about you.")
                    npc<Neutral>("Now what are these questions?")
                    questions()
                }
                option<Neutral>("Talking to cats.") {
                    npc<Neutral>("A noble and rewarding past-time.")
                    choice {
                        option<Quiz>("How did you get here?") {
                            getHere()
                        }
                        option<Neutral>("What are you doing here?") {
                            doingHere()
                        }
                        option<Quiz>("Who are you?") {
                            whoAreYou()
                        }
                    }
                }
                option<Neutral>("Nothing much.") {
                    npc<Neutral>("Yawn! Nice talking to you then, no-fur.")
                }
            }
        }
    }

    suspend fun Player.questions() {
        choice {
            option<Neutral>("What are the creatures inside the gardens?") {
                npc<Neutral>("Oh, you mean the gardeners?")
                player<Confused>("The strange, floaty creatures.")
                npc<Neutral>("Yes, the gardeners. Don't let them see you or they'll teleport you out here. They're very protective of their crops.")
                moreQuestions()
            }
            option<Neutral>("How do you get into the seasonal gardens?") {
                npc<Neutral>("I get in through gaps in the hedge. You are a little too big to squeeze through. I think you'll have to use the gates.")
                npc<Neutral>("I think the gates are locked, so you'll have to have good Thieving skills to get in.")
                moreQuestions()
            }
            option<Neutral>("How is it that the gardens are in different seasons?") {
                npc<Neutral>("How did you get here?")
                player<Confused>("Magic?")
                npc<Neutral>("Exactly. The Sorceress likes to have a good supply of in-season sq'irks and herbs at all times.")
                moreQuestions()
            }
            option<Neutral>("How do I get out of here?") {
                npc<Neutral>("Take a drink from the fountain.")
                moreQuestions()
            }
        }
    }

    suspend fun Player.moreQuestions() {
        choice {
            option<Neutral>("Thanks, I have another question though.") {
                questions()
            }
            option<Neutral>("Thanks for your help.")
        }
    }

    suspend fun Player.getHere() {
        npc<Neutral>("Every time I play with spiders in the Sorceress's house, her silly apprentice completely freaks out and teleports me here!")
        player<Neutral>("So you've been stuck here since?")
        npc<Neutral>("No, silly! I drink from the fountain whenever I want to leave.")
        anotherQuestion()
    }

    suspend fun Player.doingHere() {
        npc<Neutral>("I get this strange urge for sq'irks. It's quite peculiar. I think I may be addicted.")
        player<Neutral>("I think I know someone else who may be in a similar position.")
        npc<Neutral>("Don't tell me. Osman, right?")
        player<Confused>("You know Osman?")
        npc<Neutral>("Oh, he used to come here all the time. Then one day, he just stopped.")
        anotherQuestion()
    }

    suspend fun Player.whoAreYou() {
        npc<Neutral>("Del-Monty the cat, at your service.")
        player<Neutral>("Are you a famous adventurer who was turned into a cat by a vindictive mage?")
        npc<Neutral>("No; as I said, I'm Del-Monty the cat, connoisseur of exotic fruits.")
        player<Neutral>("In that case, can you tell me anything about sq'irks?")
        npc<Neutral>("But of course! Their juice is an excellent source of energy for runners, and in the riper varieties they are known to heighten one's Thieving abilities.")
        anotherQuestion()
    }

    suspend fun Player.anotherQuestion() {
        choice {
            option<Neutral>("Thanks, I have another question though.") {
                choice {
                    option<Quiz>("How did you get here?") {
                        getHere()
                    }
                    option<Neutral>("What are you doing here?") {
                        doingHere()
                    }
                    option<Quiz>("Who are you?") {
                        whoAreYou()
                    }
                }
            }
            option<Neutral>("Thanks for your help.")
        }
    }
}
