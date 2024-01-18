package world.gregs.voidps

import world.gregs.voidps.engine.timed
import java.util.*

fun properties(path: String) : Properties = timed("properties") {
    val properties = Properties()
    properties.load(Main::class.java.getResourceAsStream(path))
    return@timed properties
}