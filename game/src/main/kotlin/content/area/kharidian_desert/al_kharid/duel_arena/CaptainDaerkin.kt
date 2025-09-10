package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.event.Script
@Script
class CaptainDaerkin {

    init {
        npcOperate("Talk-to", "captain_daerkin") {
            npc<Happy>("Hello old chap.")
            player<Neutral>("What are you doing here? Shouldn't you be looking after your glider?")
            npc<Happy>("I'm pretty much retired these days old fellow. My test piloting days are over. I'm just relaxing here and enjoying the primal clash between man and man.")
            player<Neutral>("You're watching the duels then. Are you going to challenge someone yourself?")
            npc<Happy>("I do find the duels entertaining to watch, but I suspect that actually being involved would be a lot less fun for me. I'm a lover, not a fighter!")
            player<Neutral>("Errm, I suppose you are.")
        }

    }

}
