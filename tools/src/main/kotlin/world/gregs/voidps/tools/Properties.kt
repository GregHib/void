package world.gregs.voidps.tools

import java.io.FileInputStream
import java.util.Properties

private val properties: Properties by lazy {
    Properties().apply {
        load(FileInputStream("./game/src/test/resources/test.properties"))
    }
}

fun property(name: String): String {
    return properties.getProperty(name).replace("../", "./")
}

fun propertyOrNull(name: String): String? {
    return properties.getProperty(name)?.replace("../", "./")
}
