package content.area.misthalin.lumbridge.swamp

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player

class LostCityAdventurers : Script {
    init {
        npcOperate("Talk-to", "archer_lumbridge") {
            when (quest("lost_city")) {
                "unstarted", "started" -> {
                    player<Quiz>("Why are you guys hanging around here?")
                    npc<Angry>("(ahem)...'Guys'?")
                    player<Neutral>("Uh... yeah, sorry about that. Why are you all standing around out here?")
                    npc<Neutral>("Well, that's really none of your business.")
                }
                else -> {
                    player<Quiz>("So you didn't find the entrance to Zanaris yet, huh?")
                    npc<Angry>("Don't tell me a novice like YOU has found it!")
                    player<Neutral>("Yep. Found it REALLY easily too.")
                    npc<Confused>("...I cannot believe that someone like YOU could find the portal where experienced adventurers such as ourselves could not.")
                    player<Happy>("Believe what you want. Enjoy your little camp fire.")
                }
            }
        }

        npcOperate("Talk-to", "wizard_lumbridge") {
            when (quest("lost_city")) {
                "unstarted", "started" -> {
                    player<Quiz>("Why are all of you standing around here?")
                    npc<Happy>("Hahaha you dare talk to a mighty wizard such as myself? I bet you can't even cast windstrike yet amateur!")
                    player<Neutral>("...You're an idiot.")
                }
                else -> {
                    npc<Happy>("Hahaha you're such an amateur!")
                    npc<Happy>("Go away and play with some cabbage amateur!")
                    player<Neutral>("...right.")
                }
            }
        }

        npcOperate("Talk-to", "monk_lumbridge") {
            when (quest("lost_city")) {
                "unstarted", "started" -> {
                    player<Quiz>("Why are all of you standing around here?")
                    npc<Angry>("None of your business. Get lost.")
                }
                else -> npc<Neutral>("I already told you. I'm not talking to you anymore.")
            }
        }

        npcOperate("Talk-to", "warrior_lumbridge") {
            when (quest("lost_city")) {
                "unstarted" -> {
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
                "started" -> straight()
                "find_staff" -> {
                    player<Quiz>("Have you found anything yet?")
                    npc<Angry>("We're still searching for Zanaris...GAH! I mean we're not doing anything here at all.")
                    npc<Sad>("I haven't found it yet either.")
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
                    if (!World.members) {
                        npc<Angry>("Of course we know! We will find Zanaris, just you wait. Now go away!")
                        return@option
                    }
                    set("lost_city", "started")
                    npc<Angry>("Of course we know! We just haven't found which tree the stupid leprechaun's hiding in yet!")
                    player<Quiz>("Leprechaun?")
                    npc<Angry>("GAH! I didn't mean to tell you that! Look, just forget I said anything okay?")
                    player<Quiz>("So a leprechaun knows where Zanaris is eh?")
                    npc<Angry>("Ye.. uh, no. No, not at all. And even if he did - which he doesn't - he DEFINITELY ISN'T hiding in some tree around here. Nope, definitely not. Honestly.")
                    player<Happy>("Thanks for the help!")
                    npc<Angry>("Help? What help? I didn't help! Please don't say I did, I'll get in trouble!")
                    straight()
                }
            }
        }
    }

    private suspend fun Player.straight() {
        player<Quiz>("So let me get this straight: I need to search the trees around here for a leprechaun; and then when I find him, he will tell me where this 'Zanaris' is?")
        npc<Angry>("What? How did you know that? Uh... I mean, no, no you're very wrong. Very wrong, and not right at all, and I definitely didn't tell you about that at all.")
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
