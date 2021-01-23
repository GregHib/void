package world.gregs.voidps.tools.map.obj

val stairOptionNameOpposition: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    when(opt) {
        "climb down" -> {
            when(target.opt) {
                "climb up" -> 1.0
                "climb" -> 0.6
                else -> 0.0
            }
        }
        "climb up" -> {
            when(target.opt) {
                "climb down" -> 1.0
                "climb" -> 0.6
                else -> 0.0
            }
        }
        else -> 0.0
    }
}

val stairType: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    val name = obj.def.name.toLowerCase()
    val targetName = target.obj.def.name.toLowerCase()
    if (name.isStair() && targetName.isStair()) {
        1.0
    } else if (name.isLadder() || targetName.isLadder()) {
        0.0
    } else {
        0.8
    }
}

fun String.isStair() = contains("stair") || contains("steps")