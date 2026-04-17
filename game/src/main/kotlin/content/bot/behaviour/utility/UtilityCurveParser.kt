package content.bot.behaviour.utility

object UtilityCurveParser {

    fun parseCurve(map: Map<String, Any>): UtilityCurve {
        val type = map["type"] as? String ?: error("Curve missing 'type' in $map.")
        return when (type) {
            "linear" -> UtilityCurve.Linear(min = doubleOr(map, "min", 0.0), max = doubleOr(map, "max", 1.0))
            "exponential" -> {
                val base = map["base"] ?: error("Curve missing required field 'base' in $map.")
                UtilityCurve.Exponential(base = (base as? Number)?.toDouble() ?: error("Field 'base' must be a number in $map."))
            }
            "sine" -> UtilityCurve.Sine
            "cosine" -> UtilityCurve.Cosine
            "logistic" -> UtilityCurve.Logistic(midpoint = doubleOr(map, "midpoint", 0.5), steepness = doubleOr(map, "steepness", 1.0))
            "logit" -> UtilityCurve.Logit(midpoint = doubleOr(map, "midpoint", 0.5), steepness = doubleOr(map, "steepness", 1.0))
            "smooth_step" -> UtilityCurve.SmoothStep(min = doubleOr(map, "min", 0.0), max = doubleOr(map, "max", 1.0))
            "smoother_step" -> UtilityCurve.SmootherStep(min = doubleOr(map, "min", 0.0), max = doubleOr(map, "max", 1.0))
            else -> error("Unknown curve type '$type' in $map.")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun parseScorer(list: List<Map<String, Any>>): TargetScorer {
        val components = list.map { entry ->
            val inputKey = entry["input"] as? String ?: error("Score component missing 'input' in $entry.")
            val input = TargetInput.byKey(inputKey) ?: error("Unknown target input '$inputKey' in $entry.")
            val curveMap = entry["curve"] as? Map<String, Any> ?: error("Score component missing 'curve' map in $entry.")
            val curve = parseCurve(curveMap)
            val weight = doubleOr(entry, "weight", 1.0)
            TargetScorer.ScoreComponent(input, curve, weight)
        }
        return TargetScorer(components)
    }

    private fun doubleOr(map: Map<String, Any>, key: String, default: Double): Double {
        val value = map[key] ?: return default
        return (value as? Number)?.toDouble() ?: error("Field '$key' must be a number in $map.")
    }
}
