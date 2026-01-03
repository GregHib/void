package content.area.asgarnia.port_sarim

import content.entity.obj.ship.boatTravel
import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class SquirePortSarim : Script {
    init {
        npcOperate("Talk-to", "squire_port_sarim") {
            npc<Sad>("Well the order has become quite diminished over the years, it's a very long process to learn the skills of a Void Knight. Recently there have been breaches into our realm from somewhere else, and strange creatures")
            npc<Disheartened>("Hi, how can I help you?")
            choice {
                option<Quiz>("Who are you?") {
                    npc<Neutral>("I'm a Squire for the Void Knights.")
                    player<Quiz>("The who?")
                    npc<Happy>("The Void Knights, they are great warriors of balance who do Guthix's work here in Gielinor.")
                    choice {
                        join()
                        whatWork()
                        gielinor()
                        option<Shifty>("Uh huh, sure.")
                    }
                }
                option<Quiz>("Where does this ship go?") {
                    npc<Neutral>("To the Void Knight outpost. It's a small island just off Karamja.")
                    choice {
                        outpost()
                        option("That's nice.")
                    }
                }
                outpost()
                option<Neutral>("I'm fine thanks.")
            }
        }

        npcOperate("Travel", "squire_port_sarim") {
            travel()
        }
    }

    private fun ChoiceOption.outpost() {
        option<Neutral>("I'd like to go to your outpost.") {
            npc<Neutral>("Certainly, right this way.")
            travel()
        }
    }

    private suspend fun Player.travel() {
        boatTravel("port_sarim_to_ape_atoll", 10, Tile(2663, 2676, 1))
        statement("The ship arrives at the Void Knight outpost.")
    }

    private fun ChoiceOption.join() {
        option<Quiz>("Wow, can I join?") {
            npc<Sad>("Entry is strictly invite only, however we do need help continuing Guthix's work.")
            choice {
                whatWork()
                option<Neutral>("Good luck with that.")
            }
        }
    }

    private fun ChoiceOption.whatWork() {
        option<Quiz>("What kind of work?") {
            npc<Neutral>("Ah well you see we try to keep Gielinor as Guthix intended, it's very challenging. Actually we've been having some problems recently, maybe you could help us?")
            choice {
                option<Quiz>("Yeah ok, what's the problem?") {
                    npc<Sad>("Well the order has become quite diminished over the years, it's a very long process to learn the skills of a Void Knight. Recently there have been breaches into our realm from somewhere else, and strange creatures")
                    npc<Sad>("have been pouring through. We can't let that happen, and we'd be very grateful if you'd help us.")
                    choice {
                        option<Quiz>("How can I help?") {
                            npc<Neutral>("We send launchers from our outpost to the nearby islands. If you go and wait in the lander there that'd really help.")
                        }
                        option("Sorry, but I can't.")
                    }
                }
                gielinor()
                option<Neutral>("I'd rather not, sorry.")
            }
        }
    }

    private fun ChoiceOption.gielinor() {
        option<Quiz>("What's 'Gielinor'?") {
            npc<Neutral>("It is the name that Guthix gave to this world, so we honour him with its use.")
        }
    }
}
