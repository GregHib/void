package content.area.misthalin.lumbridge.farm

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class FredsFarmGoblin : Script {

    init {
        npcOperate("Talk-to", "goblin_machete_orange") { (target) ->
            talk(target)
        }
    }

    suspend fun Player.talk(target: NPC) {
        npc<Angry>("I kill you human!")
        target.interactPlayer(this, "Attack")
    }
}
