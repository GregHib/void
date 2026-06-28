package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.beastOfBurden

class SpiritTerrorbird : Script {
    init {
        npcOperate("Interact", "spirit_terrorbird_familiar") {
            val used = beastOfBurden.items.count { it.isNotEmpty() }
            if (used <= 8) {
                npc<Neutral>("This is a fun little walk.")
                player<Happy>("Why do I get the feeling you'll change your tune when I start loading you up with items?")
                return@npcOperate
            }
            if (used == 9) {
                npc<Neutral>("I can keep this up for hours.")
                player<Happy>("I'm glad, as we still have plenty of time to go.")
                return@npcOperate
            }
            if (used == 10) {
                npc<Neutral>("Are we going to visit a bank soon?")
                player<Happy>("I'm not sure, you still have plenty of room for more stuff.")
                npc<Neutral>("Just don't leave it too long, okay?")
                return@npcOperate
            }
            if (used == 11) {
                npc<Neutral>("Can we go to a bank now?")
                player<Happy>("Just give me a little longer, okay?")
                npc<Neutral>("That's what you said last time!")
                player<Happy>("Did I?")
                npc<Neutral>("Yes!")
                player<Happy>("Well, I mean it this time, promise.")
                return@npcOperate
            }
            npc<Neutral>("So...heavy...")
            player<Happy>("I knew you'd change your tune once you started carrying things.")
            npc<Neutral>("Can we go bank this stuff now?")
            player<Happy>("Sure. You do look like you're about to collapse.")
        }
    }
}
