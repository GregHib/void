package content.area.asgarnia.falador

import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.Tile

class WomanFalador : Script {

    private val acceptedTiles = listOf(
        Tile(2991, 3381, 0),
        Tile(2990, 3378, 0),
        Tile(2990, 3377, 0),
        Tile(2991, 3377, 0),
        Tile(2988, 3376, 0),
        Tile(2990, 3375, 0),
        Tile(2991, 3374, 0),
        Tile(2988, 3374, 0),
    )

    init {
        npcOperate("Talk-to", "woman_falador") {
            player<Happy>("Hello.")
            npc<Happy>("Greetings! Have you come to gaze in rapture at the natural beauty of Falador's parkland?")
            player<Neutral>("Um, yes, very nice. Lots of... trees and stuff.")
            npc<Happy>("Trees! I do so love trees! And flowers! And squirrels!")
            player<Confused>("Sorry, I have a strange urge to be somewhere else.")
            walkTo(acceptedTiles.random())
            npc<Sad>("Come back to me soon and we can talk again about trees!")
            player<Bored>("...")
        }
    }
}
