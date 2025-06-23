package content.area.asgarnia.taverley

import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.inject

val patrols: PatrolDefinitions by inject()

npcSpawn("nora_t_hagg") { npc ->
    val patrol = patrols.get("nora_t_hagg")
    npc.mode = Patrol(npc, patrol.waypoints)
}
