package rs.dusk.tools.map.obj

val stairOptionNameOpposition: ObjectIdentificationContext.(GameObjectOption) -> Double = { target ->
    if (opt == "climb down" && target.opt == "climb up") {
        1.0
    } else if (opt == "climb up" && target.opt == "climb down") {
        1.0
    } else {
        0.0
    }
}