package content.area.misthalin.varrock.museum

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

class HistorianMinas : Script {
    init {
        npcOperate("Talk-to", "historian_minas") {
            player<Neutral>("Hello!")
            npc<Happy>("Hello there, welcome to Varrock Museum! Can I help you?")
            choice {
                option<Neutral>("Tell me about the Museum.") {
                    npc<Neutral>("Well, as you can see we have recently expanded a great deal to cope with the influx of finds from the Dig Site. Also, of course, to prepare for the new dig we're opening soon.")
                    choice {
                        newDig()
                        someInfo()
                        otherSections()
                    }
                }
                someInfo()
                option<Neutral>("No thanks.")
            }
        }
    }

    private fun ChoiceOption.newDig(): Unit = option<Neutral>("Tell me about the new dig.") {
        npc<Happy>("Well, we recently had a large chunk of funds donated to us, so we sent out all sorts of explorers - I'm sure you'll meet them as you go around the world. People like Simon down in the desert near the pyramid as well")
        npc<Happy>("as Aristarchus in Pollnivneach, Varmen and, of course, Anna Jones. Anyway, we think we have discovered an island that has massive archaeological significance. We can't say much beyond that at this point, as we haven't")
        npc<Happy>("researched it. If you have the qualifications and help us out around the Museum, I'm sure we could offer you a place on the dig when we're ready to go.")
        player<Happy>("That would be very nice.")
        choice {
            newDig()
            someInfo()
            otherSections()
        }
    }

    private fun ChoiceOption.otherSections() {
        option<Neutral>("Tell me about other sections of the Museum.") {
            npc<Happy>("I'd be delighted to; which areas are you interested in?")
            menu()
        }
    }

    private suspend fun Player.menu() {
        choice {
            naturalHistory()
            digsiteExhibit()
            cleaningArea()
            timeline()
            previous()
        }
    }

    private fun ChoiceOption.naturalHistory() {
        option("Natural History exhibit") {
            player<Neutral>("Tell me about the Natural History exhibit.")
            npc<Happy>("Ahh, our newest expansion. The Natural History exhibit in the basement was set up to inform our citizens of the weird and wonderful diversity in our environment today. Indeed, I am sure you will recognise some of")
            npc<Happy>("the creatures down there. Hopefully, our lectures will teach you some things you didn't know about them!")
            player<Quiz>("So where does the 'history' bit come in?")
            npc<Happy>("Ahh, I see. The 'history' refers to that of the species - it doesn't necessarily mean it's died out. After all, we have human history and we're still here!")
            player<Neutral>("I guess that makes sense.")
            menu()
        }
    }

    private fun ChoiceOption.digsiteExhibit() {
        option("Dig Site exhibit") {
            player<Neutral>("Tell me about the Dig Site exhibit.")
            npc<Happy>("Ahh, those. We're slowly filling them up with the help of people who have their Earth Sciences exams completed at the Dig Site. If you have, feel free to give us a hand. We'd appreciate it.")
            menu()
        }
    }

    private fun ChoiceOption.cleaningArea() {
        option("Dig Site find cleaning area") {
            player<Neutral>("Tell me about the Dig Site find cleaning area.")
            npc<Happy>("The cleaning area, yes. Well, we have some really good archaeologists on site right now. They're cleaning up the finds that are shipped over from the Dig Site to the east of here. If you have the right qualifications, you can")
            npc<Happy>("help them clean the finds.")
            player<Quiz>("What qualifications?")
            npc<Neutral>("Level 3 Earth Science exams. You can take them over at the Exam Centre on the Dig Site.")
            player<Happy>("Oh, I already have those!")
            npc<Happy>("That's excellent. If you feel like helping us out, pop downstairs and clean up some finds for us.")
            menu()
        }
    }

    private fun ChoiceOption.timeline() {
        option("Timeline exhibit") {
            player<Neutral>("Tell me about the Timeline exhibit.")
            npc<Happy>("Ahh, my speciality. Being a historian, matters of the past are my passion. Collated on this floor and the one above are just part of my life's work. I am still working on updating and finding out more everyday, so if you find")
            npc<Happy>("any information on your travels that may be useful, please do let me know.")
            npc<Happy>("You can follow the timeline of the history of Gielinor from the creation of the world to the present day, simply by walking around this display. A wondrous journey, I think you'll agree!")
            player<Happy>("Wow, that's a lot of information. I'm sure in my travels and quests I'll pick up a few bits of information for you.")
            menu()
        }
    }

    private fun ChoiceOption.previous() {
        option("Previous...") {
            choice {
                newDig()
                someInfo()
                option<Neutral>("Tell me about other sections of the Museum.")
            }
        }
    }

    private fun ChoiceOption.someInfo(): Unit = option("I have some information which might be of use in your displays.") {
        /*
        player<Neutral>("I have some information that might be of use in your displays.")
        npc<Happy>("That's grand! We're always glad to have more facts. Now, tell me what you know...")

        player<Happy>("Well, I was asked to retrieve the Staff of Armadyl, from the Temple of Ikov, by Lucien, but instead, I helped the guardians of the staff and banished Lucien from this plane of existence, so that he could never again return.")
        player<Happy>("I saw the staff and can describe it to you if you want?")
        npc<Happy>("Yes! Please do!")
        statement("", clickToContinue = false)
        set("vm_displays", 5690047) // https://chisel.weirdgloop.org/varbs/display?varplayer=1011
        set("vm", 30842) // https://chisel.weirdgloop.org/varbs/display?varplayer=1010
        sound("vm_gain_kudos") // 3653
        npc<Happy>("That's excellent, I'll get a display put up straight away! Display number 28, end of the 2nd Age.")

        player<Happy>("You know Bob the Cat?")
        npc<Quiz>("The little black cat that used to wander around sometimes? Sure, I remember him.")
        player<Happy>("I found out a lot about his background...turns out he was once human and helped to defeat a dragonkin once!")
        npc<Shock>("Dragonkin?")
        npc<Neutral>("Tell me more!")
        player<Happy>("Well, you see, it started when Bob went missing...and involved a big sphinx in Sophanem, the City of the Dead, and me taking care of Unferth...")
        npc<Happy>("This is starting to sound familiar...")
        player<Happy>("It must...you know Reldo, the Varrock librarian - he knows all about Robert the Strong.")
        set("vm_displays", 5755583) // https://chisel.weirdgloop.org/varbs/display?varplayer=1011
        set("vm", 30847) // https://chisel.weirdgloop.org/varbs/display?varplayer=1010
        sound("vm_gain_kudos") // 3653
        npc<Quiz>("You mean that was Bob? Wow... I will get a display erected immediately about the history of this brave warrior. Display number 20, 4th Age, year 1-100.")

        player<Neutral>("I found out more about Ivandis Seergaze, one of the priestly warriors who drove back the vampyres of Morytania.")
        npc<Quiz>("What is it?")
        player<Happy>("It seems that he wasn't buried at Paterdomus with the others. His tomb is actually deep within Morytania!")
        set("vm_displays", 5756095) // https://chisel.weirdgloop.org/varbs/display?varplayer=1011
        set("vm", 30852) // https://chisel.weirdgloop.org/varbs/display?varplayer=1010
        sound("vm_gain_kudos") // 3653

        npc<Happy>("How fascinating. I'll have the display updated. Number 23, 4th Age, year 1100-1200.")
         */

        player<Neutral>("I have some information that might be of use in your displays.")
        npc<Happy>("That's grand! We're always glad to have more facts. Now, tell me what you know...")
        player<Confused>("On second thoughts, you seem to have everything I know covered here!")
    }
}
