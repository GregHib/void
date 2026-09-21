package content.area.asgarnia.falador

import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class ApprenticeWorkman : Script {
    init {
        npcOperate("Talk-to", "apprentice_workman") {
            player<Happy>("Hiya.")
            npc<Sad>("Sorry, I haven't got time to chat. We've only just finished a collossal order of furniture for the Varrock area, and already there's more work coming in.")
            player<Confused>("Varrock?")
            npc<Bored>("Yeah, the Council's had it redecorated.")
            npc("workman_falador", "angry", "Oi - stop gabbing and get that chair finished!")
            npc<Sad>("You'd better let me get on with my work.")
            player<Neutral>("Ok, bye.")
        }
    }
}
