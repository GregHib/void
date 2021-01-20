package world.gregs.void.tools.map.obj

import world.gregs.void.ai.inverse
import world.gregs.void.ai.logistic
import world.gregs.void.ai.scale
import world.gregs.void.engine.map.Distance.euclidean
import world.gregs.void.engine.map.Distance.getNearest
import world.gregs.void.engine.map.Tile

val distanceToTile: ObjectIdentificationContext.(Pair<Tile, Tile>) -> Double = { target ->
    val nearest = getNearest(obj.tile, obj.size, target.first)
    val distance = euclidean(nearest, target.first)
    if (distance >= 10.0) {
        0.0
    } else {
        distance.scale(0.0, 10.0)
            .inverse()
            .logistic(steepness = 1.27, midpoint = 0.2)
    }
}