package world.gregs.voidps.tools.map.obj.types

import world.gregs.voidps.tools.map.obj.GameObjectOption
import world.gregs.voidps.tools.map.obj.ObjectIdentificationContext

val stairOptionNameOpposition: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    when (opt) {
        "climb down" -> {
            when (target.opt) {
                "climb up" -> 1.0
                "climb" -> 0.6
                else -> 0.0
            }
        }
        "climb up" -> {
            when (target.opt) {
                "climb down" -> 1.0
                "climb" -> 0.6
                else -> 0.0
            }
        }
        else -> 0.0
    }
}

fun String.isStair() = contains("stair") || contains("steps")
