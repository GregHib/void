package content.area.misthalin.varrock.palace

import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Surprised
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate

npcOperate("Talk-to", "surok_magis") {
    npc<Frustrated>("Can't you see I'm very busy here? Be off with you!")
    player<Surprised>("Oh. Sorry.")
}
