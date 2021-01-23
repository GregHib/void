package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.ai.inverse
import world.gregs.voidps.ai.logistic
import world.gregs.voidps.ai.scale
import world.gregs.voidps.engine.map.Distance.euclidean
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Tile

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