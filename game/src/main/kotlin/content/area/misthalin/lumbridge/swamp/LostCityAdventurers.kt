package content.area.misthalin.lumbridge.swamp

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class LostCityAdventurers : Script {
    init {
        npcOperate("Talk-to", "archer_lumbridge") {
            player<Quiz>("Why are you guys hanging around here?")
            npc<Angry>("(ahem)...'Guys'?")
            player<Neutral>("Uh... yeah, sorry about that. Why are you all standing around out here?")
            npc<Neutral>("Well, that's really none of your business.")
        }

        npcOperate("Talk-to", "wizard_lumbridge") {
            player<Quiz>("Why are all of you standing around here?")
            npc<Happy>("Hahaha you dare talk to a mighty wizard such as myself? I bet you can't even cast windstrike yet amateur!")
            player<Neutral>("...You're an idiot.")
        }

        npcOperate("Talk-to", "monk_lumbridge") {
            player<Quiz>("Why are all of you standing around here?")
            npc<Angry>("None of your business. Get lost.")
        }

        npcOperate("Talk-to", "warrior_lumbridge") {
            npc<Neutral>("Hello there traveller.")
            choice {
                option("What are you camped out here for?") {
                    player<Quiz>("What are you camped here for?")
                    lookingForZanaris()
                }
                option<Quiz>("Do you know any good adventures I can go on?") {
                    npc<Neutral>("Well we're on an adventure right now. Mind you, this is OUR adventure and we don't want to share it - find your own!")
                    choice {
                        pleaseTell()
                        option<Angry>("I don't think you've found a good adventure at all!") {
                            npc<Angry>("Hah! Adventurers of our calibre don't just hang around in forests for fun, whelp!")
                            player<Quiz>("Oh really?")
                            player<Quiz>("What are you camped here for?")
                            lookingForZanaris()
                        }
                    }
                }
            }
        }
    }

    private suspend fun Player.lookingForZanaris() {
        npc<Neutral>("We're looking for Zanaris...GAH! I mean we're not here for any particular reason at all.")
        choice {
            whosZanaris()
            whatsZanaris()
            whyHere()
        }
    }

    private fun ChoiceOption.whosZanaris() {
        option<Quiz>("Who's Zanaris?") {
            npc<Neutral>("Ahahahaha! Zanaris isn't a person! It's a magical hidden city filled with treasures and rich.. uh, nothing. It's nothing.")
            choice {
                itsHidden()
                noSuchThing()
            }
        }
    }

    private fun ChoiceOption.itsHidden() {
        option<Quiz>("If it's hidden how are you planning to find it?") {
            npc<Neutral>("Well, we don't want to tell anyone else about that, because we don't want anyone else sharing in all that glory and treasure.")
            choice {
                pleaseTell()
                option("Looks like you don't know either.") {
                    player<Confused>("Well, it looks to me like YOU don't know EITHER seeing as you're all just sat around here.")
                    npc<Angry>("Of course we know! We will find Zanaris, just you wait. Now go away!")
                }
            }
        }
    }

    private fun ChoiceOption.noSuchThing() {
        option("There's no such thing.") {
            player<Angry>("There's no such thing!")
            npc<Neutral>("When we've found Zanaris you'll... GAH! I mean, we're not here for any particular reason at all.")
            choice {
                whosZanaris()
                whatsZanaris()
                whyHere()
            }
        }
    }

    private fun ChoiceOption.pleaseTell() {
        option("Please tell me.") {
            player<Quiz>("Please tell me?")
            npc<Neutral>("No.")
            player<Quiz>("Please?")
            npc<Neutral>("No!")
            player<Quiz>("PLEEEEEEEEEEEEEEEEEEEEEEASE???")
            npc<Angry>("NO!")
        }
    }

    private fun ChoiceOption.whyHere() {
        option<Quiz>("What makes you think it's out here?") {
            npc<Neutral>("Don't you know of the legends that tell of the magical city, hidden in the swam... Uh, no, you're right, we're wasting our time here.")
            choice {
                itsHidden()
                noSuchThing()
            }
        }
    }

    private fun ChoiceOption.whatsZanaris() {
        option<Quiz>("What's Zanaris?") {
            npc<Neutral>("I don't think we want other people competing with us to find it. Forget I said anything.")
            choice {
                pleaseTell()
                option<Neutral>("Oh well. Never mind.")
            }
        }
    }
}
