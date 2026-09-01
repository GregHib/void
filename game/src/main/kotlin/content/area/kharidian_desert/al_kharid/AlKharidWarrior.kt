package content.area.kharidian_desert.al_kharid

import content.entity.combat.dead
import content.entity.combat.inCombat
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class AlKharidWarrior : Script {

    init {
        npcSpawn("al_kharid_warrior") {
            clear("al_kharid_warrior_called_for_help")
            clear("al_kharid_warrior_assisted")
        }

        npcCombatStart { player ->
            if (id != "al_kharid_warrior" || player !is Player || dead || hide || queue.contains("death") || levels.get(Skill.Constitution) <= 0) {
                return@npcCombatStart
            }
            if (get("al_kharid_warrior_assisted", false)) {
                return@npcCombatStart
            }
            if (get("al_kharid_warrior_called_for_help", false)) {
                return@npcCombatStart
            }
            set("al_kharid_warrior_called_for_help", true)
            for (warrior in NPCs.at(tile.regionLevel).filter { it.id == "al_kharid_warrior" && it != this && !it.dead && !it.hide && it.levels.get(Skill.Constitution) > 0 && !it.inCombat && it.tile.within(tile, 5) }) {
                warrior["al_kharid_warrior_assisted"] = true
                warrior.say("Brother, I will help thee with this infidel!")
                warrior.interactPlayer(player, "Attack")
            }
        }

        npcDeath("al_kharid_warrior") {
            clear("al_kharid_warrior_called_for_help")
            clear("al_kharid_warrior_assisted")
        }
    }
}
