package world.gregs.voidps.tools.map.obj

open class ObjectIdentification<T : Any>(
    val name: String,
    val targets: ObjectIdentificationContext.() -> List<T>,
    val considerations: List<ObjectIdentificationContext.(T) -> Double>,
    val weight: Double = 1.0,
) {
    /**
     * Combine [weight] with all considerations into one score
     * @return score 0..1
     */
    fun score(context: ObjectIdentificationContext, target: T): Double {
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
        return result
    }

    /**
     * Selects the target with the highest score greater than [highestScore]
     */
    fun getHighestTarget(context: ObjectIdentificationContext, highestScore: Double): Triple<Double, T, ObjectIdentification<T>>? {
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
        return if (topChoice != null) Triple(highest, topChoice, this) else null
    }

    /**
     * For debugging against an individual [target]
     */
    fun getScores(context: ObjectIdentificationContext, target: T): List<Double> = considerations.map { it(context, target) }

    /**
     * For debugging
     */
    fun getScores(context: ObjectIdentificationContext) = targets(context).map { target -> Triple(target, getScores(context, target), score(context, target)) }.toList()
}
