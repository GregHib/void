package rs.dusk.tools.map.obj

import rs.dusk.ai.cosine
import rs.dusk.ai.inverse
import rs.dusk.ai.linear
import rs.dusk.ai.scale
import rs.dusk.engine.map.Distance.levenshtein
import kotlin.math.abs

/*
    Order of importance
    names > size > ids
 */

/**
 * Names with over 20 difference between names still returns 0.6
 */
val differenceBetweenNames: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    levenshtein(obj.def.name, target.obj.def.name)
        .toDouble()
        .scale(0.0, 20.0)
        .inverse()
        .cosine(steepness = 0.3, offset = 0.6)
}

val sizeDifference: ObjectIdentificationContext.(GameObjectOption) -> Double = {
    val difX = abs(obj.size.width - it.obj.size.width)
    val difY = abs(obj.size.height - it.obj.size.height)
    (difX + difY)
        .toDouble()
        .scale(0.0, 8.0)
        .inverse()
        .linear(slope = 5.0, offset = -0.8)
}

/**
 * Interesting if id's are very close but if they're not close no big deal either
 */
val differenceBetweenIds: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    abs(obj.id - target.obj.id)
        .toDouble()
        .scale(0.0, 10.0)
        .inverse()
        .linear(slope = 5.0, offset = -0.8)
}