package world.gregs.voidps.tools.definition

import world.gregs.yaml.Yaml
import java.io.File

/**
 * Goes through config files of a particular type and fills in missing
 * examines using data from rs3 json dumps
 */
object TomlExamines {
    val examines = mutableMapOf<Int, Pair<String, String>>()

    @JvmStatic
    fun main(args: Array<String>) {
        val yaml = Yaml()
        val list = yaml.load<List<Map<String, Any>>>("${System.getProperty("user.home")}\\Downloads\\npcs.json")
        for (element in list) {
            val id = element["id"] as Int
            val examine = element["examine"] as String
            val name = element["name"] as String
            if (examine.isNotBlank()) {
                examines[id] = Pair(examine, name)
            }
        }
        for (file in File("./data/").walkTopDown()) {
            if (!file.isFile || !file.name.endsWith(".npcs.toml")) {
                continue
            }
            var first = true
            val lines = mutableListOf<String>()
            var id = -1
            var stringId = ""
            var found = false
            var clone = false
            for (line in file.readLines()) {
                if (line.startsWith("id")) {
                    id = line.split("=")[1].trim().toInt()
                } else if (line.startsWith("examine")) {
                    found = true
                } else if (line.startsWith("clone")) {
                    clone = true
                } else if (line.startsWith("[")) {
                    if (!found && !first) {
                        notFound(lines, stringId, id, clone)
                    }
                    first = false
                    stringId = line
                    found = false
                    clone = false
                }
                lines.add(line)
            }
            if (!found && !first) {
                lines.add("")
                notFound(lines, stringId, id, clone)
            }
            if (lines.lastOrNull()?.trim() != "") {
                lines.add("")
            }
            file.writeText(lines.joinToString(System.lineSeparator()))
        }
    }

    private fun notFound(lines: MutableList<String>, stringId: String, id: Int, clone: Boolean) {
        if (clone || stringId.endsWith("_noted]") || stringId.endsWith("_lent]")) {
            println("Skip clone $stringId $id")
            return
        }
        val (examine, name) = examines[id] ?: return
        if (examine == "" || examine == "null") {
            return
        }
        val last = lines.removeLast()
        println("Found examine: $stringId $name = \"$examine\"")
        lines.add("examine = \"$examine\"")
        lines.add(last)
    }
}