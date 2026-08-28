package content.area.asgarnia.falador

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name

class Narf : Script {
    init {
        npcOperate("Talk-to", "narf") {
            player<Idle>("That's a funny name you've got.")
            npc<Angry>("'Narf'? You think that's funny? At least I don't call myself $name! Where did you get a name like that?")
            player<Sad>("It seemed like a good idea at the time!")
            npc<Angry>("Bah!")
        }
    }
}
