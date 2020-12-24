package rs.dusk.ai

interface Option {
    val targets: Context.() -> List<Any>
    val considerations: Set<Context.(Any) -> Double>
    val momentum: Double
    val weight: Double

    /**
     * Combine [weight] with all considerations into one score
     * @return score 0..1 + [momentum]
     */
    fun score(context: Context, target: Any): Double {
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
    fun getHighestTarget(context: Context, highestScore: Double): Decision? {
        var highest = highestScore
        var topChoice: Any? = null
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
        return if (topChoice != null) Decision(topChoice, this, highest) else null
    }
}