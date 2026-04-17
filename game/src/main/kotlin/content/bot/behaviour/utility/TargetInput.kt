package content.bot.behaviour.utility

import content.bot.behaviour.perception.BotCombatContext
import content.entity.combat.attacker
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill

sealed interface TargetInput {
    val key: String
    fun value(scorer: Player, target: Player, context: BotCombatContext): Double

    object TargetHpPercent : TargetInput {
        override val key = "target_hp_percent"
        override fun value(scorer: Player, target: Player, context: BotCombatContext): Double {
            val max = target.levels.getMax(Skill.Constitution)
            if (max <= 0) return 0.0
            return target.levels.get(Skill.Constitution).toDouble() / max
        }
    }

    object Distance : TargetInput {
        override val key = "distance"
        override fun value(scorer: Player, target: Player, context: BotCombatContext): Double = scorer.tile.distanceTo(target.tile).toDouble()
    }

    object CombatLevelDelta : TargetInput {
        override val key = "combat_level_delta"
        override fun value(scorer: Player, target: Player, context: BotCombatContext): Double = (target.combatLevel - scorer.combatLevel).toDouble()
    }

    object AttackerOfAlly : TargetInput {
        override val key = "attacker_of_ally"
        override fun value(scorer: Player, target: Player, context: BotCombatContext): Double {
            for (ally in context.nearbyAllies) {
                if (ally === scorer) continue
                if (ally.attacker === target) return 1.0
            }
            return 0.0
        }
    }

    companion object {
        private val all = listOf(TargetHpPercent, Distance, CombatLevelDelta, AttackerOfAlly)
        private val byKey = all.associateBy { it.key }
        fun byKey(key: String): TargetInput? = byKey[key]
    }
}
