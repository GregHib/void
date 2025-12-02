package content.entity.npc.combat.melee

import content.entity.combat.killer
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.queue.strongQueue

class LumbridgeGuardsmanAttackble : Script {
    init {
        huntNPC("aggressive_npcs") { target ->
            if (id == "lumbridge_guardsman_attackable2" && target.id.endsWith("rat")) {
                interactNpc(target, "Attack")
            }
            if (id == "lumbridge_guardsman_attackable" && target.id.contains("goblin")) {
                interactNpc(target, "Attack")
            }
        }
        npcDeath("goblin*,giant_rat") {
            val guard = killer as? NPC ?: return@npcDeath
            when {
                // Guard attacked rats and giant rats, but only healed when giant rat was killed https://youtu.be/qC_XoAQK6Cs?si=VH9tOmKXu-_Bgjnr&t=137
                id == ("giant_rat") && guard.id == "lumbridge_guardsman_attackable2" -> {
                    guard.strongQueue("rat_killer") {
                        guard.delay(2)
                        guard.anim("eat_drink")
                        // From wiki: Every time they kill a monster, they eat a piece of food which heals them to full health, and walk over to the square of their death as to collect a drop.
                        guard.levels.set(Skill.Constitution, guard.levels.getMax(Skill.Constitution))
                        guard.walkTo(tile)
                    }
                }
                id.contains("goblin") && guard.id == "lumbridge_guardsman_attackable" -> {
                    guard.strongQueue("goblin_killer") {
                        guard.delay(2)
                        guard.anim("eat_drink")
                        guard.levels.set(Skill.Constitution, guard.levels.getMax(Skill.Constitution))
                        guard.walkTo(tile)
                    }
                }
            }
        }
    }
}
