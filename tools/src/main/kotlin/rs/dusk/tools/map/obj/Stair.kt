package rs.dusk.tools.map.obj

import rs.dusk.ai.inverse
import rs.dusk.ai.logistic
import rs.dusk.ai.scale
import rs.dusk.ai.toDouble
import rs.dusk.engine.map.Distance.euclidean

/**
 * More lenient of distance than for ladders
 */
val stairDistance: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    euclidean(obj.tile, target.obj.tile)
        .scale(0.0, 20.0)
        .inverse()
        .logistic(midpoint = -0.37)
}

val stairOptionNameOpposition: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    when (opt) {
        "climb down" -> (target.opt == "climb up" || target.opt == "climb").toDouble()
        "climb up" -> (target.opt == "climb down" || target.opt == "climb").toDouble()
        else -> 0.0
    }
}