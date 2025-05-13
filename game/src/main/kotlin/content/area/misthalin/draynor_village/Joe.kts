package content.area.misthalin.draynor_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceBuilder
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

npcOperate("Talk-to", "lady_keli") {
    when(player["prince_ali_rescue", "unstarted"]) {
         "equipment" -> { // TODO

        }
        else -> {
            player<Quiz>("Hi. Who are you guarding here?")
            npc<Angry>("Can't say. It's all very secret. You should get out of here. I am not supposed to talk while I guard.")
        }
    }
}