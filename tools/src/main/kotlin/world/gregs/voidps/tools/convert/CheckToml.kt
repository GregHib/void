package world.gregs.voidps.tools.convert

import world.gregs.config.Config
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles

object CheckToml {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val actual = mutableMapOf<String, Map<String, Any>>()
        val files = configFiles()
        val paths = files.getValue(Settings["definitions.objects"])
        for (path in paths) {
            Config.fileReader(path, 512) {
                while (nextSection()) {
                    val section = section()
                    val map = mutableMapOf<String, Any>()
                    while (nextPair()) {
                        val key = key()
                        val value = value()
                        map[key] = value
                    }
                    actual[section] = map
                }
            }
        }

        val expected = mutableMapOf<String, Map<String, Any>>()

        Config.fileReader("./all.objs.toml", 512) {
            while (nextSection()) {
                val section = section()
                val map = mutableMapOf<String, Any>()
                while (nextPair()) {
                    val key = key()
                    val value = value()
                    map[key] = value
                }
                expected[section] = map
            }
        }

        for (key in expected.keys) {
            val value = expected[key]

            val act = actual[key]
            if (act != value) {
                println("Difference: $act expected: $value")
            }
        }
    }
}