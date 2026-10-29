package content.area.fremennik_province.keldagrim

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs

class ChaosDwarfBattlefield : Script {

    init {
        huntNPC(mode = "black_guard_battlefield") { target ->
            interactNpc(target, "Attack")
        }

        huntNPC(mode = "chaos_dwarf_battlefield") { target ->
            interactNpc(target, "Attack")
        }

        combatAttack { (target) ->
            if (target !is NPC || tile !in Areas["chaos_dwarf_battlefield"]) {
                return@combatAttack
            }
            // Only the dwogre being attacked fights back
            if (target.id.startsWith("chaos_dwogre")) {
                return@combatAttack
            }
            val side = side(target) ?: return@combatAttack
            for (npc in NPCs.at(tile.regionLevel)) {
                if (npc.hide || side(npc) != side || npc.mode.let { it is CombatMovement && it.target == this }) {
                    continue
                }
                npc.interactPlayer(this, "Attack")
            }
        }
    }

    private fun side(npc: NPC): String? = when {
        !npc.id.endsWith("_chaos_dwarf_battlefield") && !npc.id.contains("_chaos_dwarf_battlefield_") -> null
        npc.id.startsWith("black_guard") -> "black_guard"
        npc.id.startsWith("chaos_") -> "chaos"
        else -> null
    }
}
