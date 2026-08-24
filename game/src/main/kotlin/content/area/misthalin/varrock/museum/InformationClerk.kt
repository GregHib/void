package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class InformationClerk : Script {
    init {
        npcOperate("Talk-to", "information_clerk") {
            npc<Happy>("Welcome to Varrock Museum. How can I help you today?")
            choice("What would you like to do?") {
                map()
                digsite()
                timeline()
                naturalHistory()
                kudos()
            }
        }
    }

    private fun ChoiceOption.digsite(): Unit = option("Find out about the Dig Site exhibit.") {
        player<Quiz>("Could you tell me about the Dig Site exhibit please?")
        npc<Neutral>("Of course. The Dig Site exhibit has several display cases of finds discovered on the Dig Site to the east of Varrock.")
        npc<Happy>("I see you've already helped out in the Dig Site exhibit. Our picture of life back in the 3rd and 4th Ages in that old city is really becoming clearer. There are still a few displays that need updating, though. If you'd like to")
        npc<Happy>("know more about cleaning finds, just ask the archaeologists.")
        choice {
            somethingElse()
            option("Bye")
        }
    }

    private fun ChoiceOption.somethingElse(): Unit = option("Ask about something else.") {
        choice("What would you like to do?") {
            map()
            digsite()
            timeline()
            naturalHistory()
            kudos()
        }
    }

    private fun ChoiceOption.map(): Unit = option("Take a map of the Museum.") {
        inventory.add("museum_map")
        sound("pick")
        item("museum_map", "You reach and take a map of the Museum.")
    }

    private fun ChoiceOption.naturalHistory(): Unit = option("Find out about the Natural History exhibit.") {
        player<Quiz>("Could you tell me about the Natural History exhibit please?")
        npc<Neutral>("Why, yes. The Natural History exhibit has displays of various creatures you can find around Gielinor.")
        npc<Happy>("You really know your stuff when it comes to animals, don't you! Orlando tells me you have exceeded his teachings. For now, perhaps you should investigate the rest of the Museum.")
        choice {
            option("But what's Natural History got to do with existing animals?") {
                player<Quiz>("But what's natural history got to do with existing animals?")
                npc<Neutral>("The study of natural history is simply the study of the history of the species. The species doesn't necessarily need to be an extinct one.")
                choice {
                    somethingElse()
                    option("Bye")
                }
            }
            somethingElse()
            option("Bye")
        }
    }

    private fun ChoiceOption.timeline(): Unit = option("Find out about the Timeline exhibit.") {
        player<Quiz>("Could you tell me about the Timeline exhibit please?")
        npc<Neutral>("Why, yes. The Timeline exhibit has lots of display cases showing things from the beginning of time right up to the present day.")
        npc<Happy>("I know you've helped out a bit in the Timeline exhibit upstairs, but I'm sure you can help more. When you're out on your travels being a brave adventurer, remember that you can come back to the Museum")
        npc<Happy>("after some quests to let us know important historical facts. This will help us to update the displays and make the Museum a more informative place! You'll earn yourself Kudos too.")
        player<Happy>("Okay, thanks. One more question: why are the display numbers all out of sequence?")
        npc<Happy>("Ahh, that's due to the numbering being done as we were constructing the cases and putting the displays in them, then shuffling them into the right places. We thought rather than renumbering them all - such a")
        npc<Happy>("boring job, writing labels - we'd leave it. They all have unique numbers and future displays would mess up the consecutive numbering anyway.")
        player<Happy>("Ahhh, I see.")
        choice {
            somethingElse()
            option("Bye")
        }
    }

    private fun ChoiceOption.kudos(): Unit = option("Find out about Kudos.") {
        player<Quiz>("What is Kudos?")
        npc<Neutral>("Kudos is a measure of how much you've assisted the Museum. The more information you give us, Dig Site finds that you clean and quizzes you solve, the higher your Kudos.")
        player<Quiz>("But what's it for?")
        npc<Neutral>("Well, recently we found a rather interesting island to the north of Morytania. We believe that it may be of archaeological significance. I suspect we'll be looking for qualified archaeologists once we have constructed our")
        npc<Neutral>("canal and barge. So, we're using Kudos as a measure of who is willing and able to help us here at the Museum, so they can then be invited on our dig on the new island.")
        player<Quiz>("Would I qualify, then?")
        npc<Happy>("Why yes! You've helped us so much around the Museum and you have the necessary qualifications from the Earth Sciences exams you took. When the canal is ready, we'll let you know.")
        player<Happy>("Thank you, I'll look forward to it!")
    }
}
