package content.area.kharidian_desert.pollnivneach.dungeon

import content.entity.combat.killer
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.queue.queue

class DesertSlayerBosses : Script {
    init {
        npcDespawn("monstrous_cave_crawler,turoth_mightiest,basilisk_boss,kurask_overlord") {
            val killer = killer as? Player ?: return@npcDespawn
            killer["killed_$id"] = true
            killer.exp(Skill.Slayer, 1000.0)
            killer.tele(Tables.tile("desert_dungeon_boss.$id.exit"))
            killer.queue("shift_back") {
                killer.statement("You shift back to reality, having defeated this boss. You may now pass this barrier freely.")
            }
        }
    }
}
