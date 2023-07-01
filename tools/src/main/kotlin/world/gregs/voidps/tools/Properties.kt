package world.gregs.voidps.tools

import java.io.File


fun property(name: String): String {
    return File("./game/src/test/resources/test.properties")
        .readLines()
        .first { it.startsWith(name) }
        .split("=")[1].replace("../", "./")
}

fun propertyOrNull(name: String): String? {
    return (File("./game/src/test/resources/test.properties")
        .readLines()
        .firstOrNull { it.startsWith(name) } ?: return null)
        .split("=")[1].replace("../", "./")
}
