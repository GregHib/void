package world.gregs.voidps.tools.map.obj.types

import world.gregs.voidps.engine.map.Distance.euclidean
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.tools.map.obj.ObjectIdentificationContext
import world.gregs.voidps.tools.map.obj.inverse
import world.gregs.voidps.tools.map.obj.logistic
import world.gregs.voidps.tools.map.obj.scale

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