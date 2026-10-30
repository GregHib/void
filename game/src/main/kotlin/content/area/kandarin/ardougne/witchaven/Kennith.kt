package content.area.kandarin.ardougne.witchaven

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.male

class Kennith : Script {

    init {
        // https://runescape.wiki/w/Transcript:Sea_Slug (Post-quest dialogue)
        npcOperate("Talk-to", "kennith_fishing_platform_invisible") {
            val title = if (male) "mister" else "miss"
            player<Neutral>("Hello there, Kennith. How are you today?")
            npc<Happy>("Great $title! Thanks again for rescuing me.")
            player<Happy>("It was my pleasure Kennith. You take care now.")
            npc<Happy>("Sure $title. Goodbye now.")
        }
    }
}
