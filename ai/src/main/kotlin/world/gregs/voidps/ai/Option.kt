package world.gregs.voidps.ai

interface Option<C : Context, T : Any> {
    val targets: C.() -> List<T>
    val considerations: List<C.(T) -> Double>
    val momentum: Double
    val weight: Double
    val action: (C.(T) -> Unit)?

    /**
     * Combine [weight] with all considerations into one score
     * @return score 0..1 + [momentum]
     */
    fun score(context: C, target: T): Double {
        val compensationFactor = 1.0 - (1.0 / considerations.size)
        var result = weight
        for (consideration in considerations) {
            var finalScore = consideration(context, target)
            val modification = (1.0 - finalScore) * compensationFactor
            finalScore += (modification * finalScore)
            result *= finalScore
            if (result == 0.0) {
                return result
            }
        }

        if (this == context.last?.option) {
            result *= momentum
        }
        return result
    }

    /**
     * Selects the target with the highest score greater than [highestScore]
     */
    fun getHighestTarget(context: C, highestScore: Double): Decision<C, T>? {
        var highest = highestScore
        var topChoice: T? = null
        val targets = targets(context)
        for (target in targets) {
            if (highest > weight) {
                return null
            }

            val score = score(context, target)
            if (score > highest) {
                highest = score
                topChoice = target
            }
        }
        return if (topChoice != null) Decision(context, topChoice, this, highest) else null
    }

    /**
     * For debugging against an individual [target]
     */
    fun getScores(context: C, target: T): List<Double> = considerations.map { it(context, target) }

    /**
     * For debugging
     */
    fun getScores(context: C) = targets(context).map { target -> Triple(target, getScores(context, target), score(context, target)) }.toList()

}