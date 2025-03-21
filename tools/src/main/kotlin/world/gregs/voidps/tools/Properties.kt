package world.gregs.voidps.tools

import java.io.FileInputStream
import java.util.Properties

private val properties: Properties by lazy {
    Properties().apply {
        load(FileInputStream("./game/src/test/resources/game.properties"))
        for ((key, value) in this) {
            if(key == "storage.players.path") {
                this[key] = "../data/test-saves/"
            } else if (value is String && value.startsWith("./")) {
                this[key] = value.replace("./", "../")
            }
        }
    }
}

fun property(name: String): String {
    return properties.getProperty(name).replace("../", "./")
}

fun propertyOrNull(name: String): String? {
    return properties.getProperty(name)?.replace("../", "./")
}
