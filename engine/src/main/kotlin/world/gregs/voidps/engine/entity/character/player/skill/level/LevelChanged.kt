package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.dispatch.ListDispatcher
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
        var playerDispatcher = ListDispatcher<LevelChanged>()
        var npcDispatcher = ListDispatcher<LevelChanged>()

        override fun levelChanged(player: Player, skill: Skill, from: Int, to: Int) {
            for (instance in playerDispatcher.instances) {
                instance.levelChanged(player, skill, from, to)
            }
        }

        override fun levelChanged(npc: NPC, skill: Skill, from: Int, to: Int) {
            for (instance in npcDispatcher.instances) {
                instance.levelChanged(npc, skill, from, to)
            }
        }
    }
}