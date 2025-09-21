package content.area.asgarnia.port_sarim

import content.entity.obj.ship.boatTravel
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Upset
import content.entity.player.dialogue.type.ChoiceBuilder
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Tile

@Script
class SquirePortSarim {
    init {
        npcOperate("Talk-to", "squire_port_sarim") {
            npc<Talk>("Hi, how can I help you?")
            choice {
                option<Quiz>("Who are you?") {
                    npc<Talk>("I'm a Squire for the Void Knights.")
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
                    npc<Talk>("To the Void Knight outpost. It's a small island just off Karamja.")
                    choice {
                        outpost()
                        option("That's nice.")
                    }
                }
                outpost()
                option<Talk>("I'm fine thanks.")
            }
        }

        npcOperate("Travel", "squire_port_sarim") {
            travel()
        }
    }

    private fun ChoiceBuilder<NPCOption<Player>>.outpost() {
        option<Talk>("I'd like to go to your outpost.") {
            npc<Talk>("Certainly, right this way.")
            travel()
        }
    }

    private suspend fun NPCOption<Player>.travel() {
        boatTravel("port_sarim_to_ape_atoll", 10, Tile(2663, 2676, 1))
        statement("The ship arrives at the Void Knight outpost.")
    }

    private fun ChoiceBuilder<NPCOption<Player>>.join() {
        option<Quiz>("Wow, can I join?") {
            npc<Upset>("Entry is strictly invite only, however we do need help continuing Guthix's work.")
            choice {
                whatWork()
                option<Talk>("Good luck with that.")
            }
        }
    }

    private fun ChoiceBuilder<NPCOption<Player>>.whatWork() {
        option<Quiz>("What kind of work?") {
            npc<Talk>("Ah well you see we try to keep Gielinor as Guthix intended, it's very challenging. Actually we've been having some problems recently, maybe you could help us?")
            choice {
                option<Quiz>("Yeah ok, what's the problem?") {
                    npc<Upset>("Well the order has become quite diminished over the years, it's a very long process to learn the skills of a Void Knight. Recently there have been breaches into our realm from somewhere else, and strange creatures")
                    npc<Upset>("have been pouring through. We can't let that happen, and we'd be very grateful if you'd help us.")
                    choice {
                        option<Quiz>("How can I help?") {
                            npc<Talk>("We send launchers from our outpost to the nearby islands. If you go and wait in the lander there that'd really help.")
                        }
                        option("Sorry, but I can't.")
                    }
                }
                gielinor()
                option<Talk>("I'd rather not, sorry.")
            }
        }
    }

    private fun ChoiceBuilder<NPCOption<Player>>.gielinor() {
        option<Quiz>("What's 'Gielinor'?") {
            npc<Talk>("It is the name that Guthix gave to this world, so we honour him with its use.")
        }
    }
}