package content.area.misthalin.barbarian_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class Hunding : Script {

    // Had to use Rs3 source for this. I could not find the 2011 source so not sure if authentic. Osrs had the old Gunthor and not Haakon, which they replaced when Gunnar's Ground quest was released.
    init {
        npcOperate("Talk-to", "hunding_barbarian_village") {
            npc<Angry>("What business do you have in our village, outerlander?")
            choice {
                option<Pleased>("I'm just exploring.") {
                    npc<Frustrated>("What have you found is powerful tribe to make your soft people tremble.")
                    choice {
                        option<Neutral>("Tell me about your people.") {
                            npc<Frustrated>("We are the Fremennik. We came from the west a century ago in righteous war to purge the heresy of magic form this decadent land.")
                            npc<Frustrated>("Now we await the call to march to war once more! We are always ready for that day!")
                        }
                        option<Angry>("You look like primitive savages.") {
                            npc<Angry>("You look like an arrogant fool. Which of us is the primitive.")
                        }
                        goodBye()
                    }
                }
                option<Happy>("I'm looking for a fight!") {
                    npc<Laugh>("Go down to the longhouse and you'll find all the fighting you can handle, outerlander. Haakon will give you a rousing welcome!")
                }
                goodBye()
            }
        }
    }

    fun ChoiceOption.goodBye() {
        option<Neutral>("Goodbye.")
    }
}
