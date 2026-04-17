package content.bot.behaviour.utility

import content.bot.behaviour.perception.BotCombatContext
import world.gregs.voidps.engine.entity.character.player.Player

data class TargetScorer(val components: List<ScoreComponent>) {

    data class ScoreComponent(val input: TargetInput, val curve: UtilityCurve, val weight: Double)

    fun score(scorer: Player, target: Player, context: BotCombatContext): Double {
        var total = 0.0
        for (component in components) {
            val raw = component.input.value(scorer, target, context)
            total += component.curve.score(raw) * component.weight
        }
        return total
    }

    fun pick(scorer: Player, candidates: List<Player>, context: BotCombatContext): Player? {
        var best: Player? = null
        var bestScore = Double.NEGATIVE_INFINITY
        for (candidate in candidates) {
            val score = score(scorer, candidate, context)
            if (score > bestScore) {
                bestScore = score
                best = candidate
            }
        }
        return best
    }
}
