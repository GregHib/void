package content.area.misthalin.varrock.palace

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.Frustrated
import world.gregs.voidps.world.interact.dialogue.Surprised
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "surok_magis") {
    npc<Frustrated>("Can't you see I'm very busy here? Be off with you!")
    player<Surprised>("Oh. Sorry.")
}