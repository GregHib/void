package content.area.kharidian_desert.pollnivneach.dungeon

import content.entity.gfx.areaGfx
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.random

class MightiestTuroth : Script {
    init {
        npcCombatDamage("turoth_mightiest") {
            if (random.nextInt(5) == 0) {
                return@npcCombatDamage
            }
            val target = it.source as? Player ?: return@npcCombatDamage
            for (i in 0 until 3) {
                if (contains("swarming_$i")) {
                    continue
                }
                val tile = Areas["mightiest_turoth_boss"].random(this) ?: return@npcCombatDamage
                areaGfx("turoth_minion_spawn", tile)
                val npc = NPCs.add("turoth_swarming", tile)
                npc.interactPlayer(target, "Attack")
                set("swarming_$i", true)
                break
            }
        }
    }
}