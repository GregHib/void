package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * Notification when current skill level has changed.
 * @see [MaxLevelChanged]
 */
interface LevelChanged {
    fun levelChanged(player: Player, skill: Skill, from: Int, to: Int) {}
    fun levelChanged(npc: NPC, skill: Skill, from: Int, to: Int) {}

    companion object : LevelChanged {
        var playerDispatcher = MapDispatcher<LevelChanged>("@SkillId", "")
        var npcDispatcher = MapDispatcher<LevelChanged>("@SkillId", "@Id", "")

        override fun levelChanged(player: Player, skill: Skill, from: Int, to: Int) {
            playerDispatcher.forEach("Skill.${skill.name}", "*") { instance ->
                instance.levelChanged(player, skill, from, to)
            }
        }

        override fun levelChanged(npc: NPC, skill: Skill, from: Int, to: Int) {
            val name = "Skill.${skill.name}"
            npcDispatcher.forEach("$name:${npc.id}", name, "*") { instance ->
                instance.levelChanged(npc, skill, from, to)
            }
        }
    }
}