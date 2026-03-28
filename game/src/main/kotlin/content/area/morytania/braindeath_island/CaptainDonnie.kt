package content.area.morytania.braindeath_island

import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script

class CaptainDonnie : Script {
    init {
        npcOperate("Talk-to", "captain_donnie") {
            if (!questCompleted("rum_deal")) {
                return@npcOperate
            }
            npc<Drunk>("Hey! You!")
            player<Quiz>("Who? Me?")
            npc<Drunk>("Aye! Ye!")
            npc<Drunk>("Got any more rum?")
            player<Shifty>("What happens if I don't?")
            npc<Drunk>("Then I'll clap ye in Runes!")
            player<Quiz>("Don't you mean clap me in irons?")
            npc<Drunk>("No lad, not irons, Runes!")
            npc<Drunk>("We upgraded last week.")
            player<Shifty>("Ok, well it's a good job that I have some over here isn't it?")
            npc<Drunk>("Arr, yer a good lad...")
        }
    }
}