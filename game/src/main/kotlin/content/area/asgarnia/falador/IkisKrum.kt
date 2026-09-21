package content.area.asgarnia.falador

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.male

class IkisKrum : Script {
    init {
        npcOperate("Talk-to", "ikis_krum") {
            player<Neutral>("Hello.")
            npc<Confused>("Good day, ${if (male) "sir" else "madam"}. What brings you to this end of town?")
            player<Neutral>("Well, what is there to do around here?")
            npc<Neutral>("If you're into Mining, plenty! The dwarves have one of the largest mines in the world just under our feet. There's an entrance in the building just north-east of my house.")
            npc<Angry>("If you're one of these young, loud, hipster sorts you could visit the Party Room, just north of here. That blasted Pete parties all night and I never get any sleep!")
            player<Confused>("Thanks.")
        }
    }
}
