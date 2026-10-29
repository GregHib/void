package content.quest.member.observatory_quest

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class ObservatoryProfessor : Script {

    init {
        // https://runescape.wiki/w/Transcript:Observatory_professor
        npcOperate("Talk-to", "observatory_professor_observatory_3") {
            npc<Neutral>("What would you like to talk about?")
            choice {
                option("Talk about the Observatory Quest.") {
                    observatory()
                }
                option("Talk about Treasure Trails.") {
                    treasureTrails()
                }
            }
        }
    }

    private suspend fun Player.observatory() {
        npc<Happy>("Hello, friend.")
        npc<Neutral>("Welcome back.")
        npc<Neutral>("Thanks for all your help with the telescope.")
        npc<Quiz>("What can I do for you?")
        choice {
            option<Quiz>("Do you need any more help with the telescope?") {
                npc<Neutral>("Not right now,")
                npc<Neutral>("but the stars may hold a secret for you.")
            }
            option<Neutral>("Nothing, thanks.") {
                npc<Neutral>("Okay, no problem. See you again.")
            }
        }
    }

    private suspend fun Player.treasureTrails() {
        val tools = inventory.contains("sextant") && inventory.contains("watch")
        when {
            !tools -> {
                player<Quiz>("Can you teach me to solve Treasure Trails clues?")
                npc<Neutral>("Ah, I get asked about Treasure Trails all the time! Listen carefully and I shall tell you what I know. Lots of clues have degrees and minutes written on them. These are coordinates of the place where the treasure is buried. You will have to walk to the correct spot, so that your coordinates are exactly the same as the values written on the clue scroll. To do this, you must use a sextant, a watch and a chart to find the coordinates of where you are. Once you know the coordinates of your position, you know which way you have to walk to get to the treasure's coordinates!")
                player<Neutral>("Riiight. So, where do I get these items from?")
                npc<Neutral>("I think Murphy, the owner of the Fishing Trawler moored at Port Khazard, might be able to spare you a sextant. After that, the nearest clock tower is south of Ardougne - you could probably get a watch there. I've got plenty of charts myself; just come back here when you've got the sextant and watch, and I'll give you one and teach you how to use them.")
                player<Neutral>("Thanks, I'll see you later.")
            }
            !inventory.contains("chart") -> {
                player<Happy>("I've got the sextant and watch!")
                npc<Happy>("Well done! You use the sextant to measure the angle that the sun is currently at. You need the watch so that you know what the time is back here at the Observatory. You then need this chart to work out your position. Your position is recorded in terms of latitude and longitude. Latitude is your position above the equator and longitude is your position relative to here.")
                if (!inventory.add("chart")) {
                    return
                }
                item("chart", "The professor has given you a navigation chart.")
                npc<Neutral>("So, if you have your sextant, watch and chart with you, then you can work out exactly where you are!")
            }
            else -> {
                npc<Quiz>("How can I help you today?")
                player<Quiz>("Can you remind me how to navigate?")
                npc<Neutral>("Certainly, it's not difficult.")
                npc<Neutral>("Lots of clues have degrees and minutes written on them. These are the coordinates of where the treasure is buried.")
                npc<Neutral>("You have to walk to the correct spot so that your coordinates are exactly the same as the values written on the clue scroll.")
                npc<Neutral>("To do this, you must use a sextant, a watch and a chart to find the coordinates of where you are.")
                npc<Neutral>("Once you know the coordinates of your position, you know which way you have to walk to get to the treasure's coordinates.")
            }
        }
    }
}
