package content.quest.member.fairy_tale_part_2.hideout

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name

class HideoutFairyQueen : Script {
    init {
        // https://runescape.wiki/w/Transcript:Fairy_Queen#Betweeen_Fairy_Tale_II_and_III
        npcOperate("Talk-to", "fairy_queen_fairy_hq_2") {
            player<Quiz>("Have you managed to work out a plan yet?")
            npc<Neutral>("Not yet, $name, but it looks like we'll need to ask you for your help again, I'm afraid. I'll be able to tell you more once we have finished making our battle plans.")
        }
    }
}
