package world.gregs.voidps.tools.map.obj.types

import world.gregs.voidps.tools.map.obj.ObjectIdentificationContext
import world.gregs.voidps.type.Distance.euclidean
import world.gregs.voidps.type.Distance.getNearest
import world.gregs.voidps.type.Tile

val distanceToTile: ObjectIdentificationContext.(Pair<Tile, Tile>) -> Double = { target ->
    val nearest = getNearest(obj.tile, obj.width, obj.height, target.first)
    val distance = euclidean(nearest, target.first)
    if (distance >= 10.0) {
        0.0
    } else {
        distance.scale(0.0, 10.0)
            .inverse()
            .logistic(steepness = 1.27, midpoint = 0.2)
    }
}
