package content.area.asgarnia.falador

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class ManFalador2 : Script {
    init {
        npcOperate("Talk-to", "man_falador_2") {
            player<Neutral>("Hello there.")
            npc<Sad>("Oh, hello. I don't suppose you've seen a campsite around here, have you?")
            player<Neutral>("No, sorry.")
            npc<Neutral>("Curse this lousy sense of direction. All my stuff is at that campsite!")
            player<Confused>("You've still got what you're carrying, though.")
            npc<Neutral>("Well, yes. Not everything else of mine, though.")
        }
    }
}
