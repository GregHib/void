package content.skill.dungeoneering

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.entity.npc.markHint
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.Approachable
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class DungeonMark : Script {
    init {
        Approachable.npcMark = { target ->
            markNpc(target)
        }
    }

    private fun Player.markNpc(target: NPC) {
        if (dungeonLeader != this) {
            message("Only your party's leader can mark a target!")
            return
        }
        for (member in dungeonMembers) {
            target.markHint(member)
        }
    }
}
