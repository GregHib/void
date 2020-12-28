package rs.dusk.tools.map.obj

import rs.dusk.ai.inverse
import rs.dusk.ai.logistic
import rs.dusk.ai.scale
import rs.dusk.engine.map.Distance.euclidean
import rs.dusk.engine.map.Distance.getNearest
import rs.dusk.engine.map.Tile

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