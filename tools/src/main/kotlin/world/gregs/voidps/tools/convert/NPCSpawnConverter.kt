package world.gregs.voidps.tools.convert

object NPCSpawnConverter {

    @JvmStatic
    fun main(args: Array<String>) {
        val string = """
{{LocLine
|name = Pyrefiend
|location = [[God Wars Dungeon]]
|levels = 48
|members = Yes
|mapID = 7
|plane = 0
|x:2863,y:5307|x:2864,y:5280|x:2864,y:5281|x:2873,y:5312
|mtype = pin
|leagueRegion = asgarnia
}}
        """.trimIndent()
        val lines = string.lines()
        for (i in lines.indices) {
            val line = lines[i]
            if (line.startsWith("{{LocLine")) {
                process(i, lines)
            }
        }
    }

    fun process(index: Int, lines: List<String>) {
        var i = index + 1
        var name = ""
        var members = false
        var level = 0
        while (i < lines.size) {
            val line = lines[i]
            if (!line.startsWith("|")) {
                break
            }
            if (line.startsWith("|x:") || line.startsWith("|npcid:")) {
                for (entry in line.replace("}}", "").split("|").drop(1)) {
                    val parts = entry.split(",", ":")
                    if (parts.size <= 1) {
                        continue
                    }
                    val index = parts.indexOf("x")
                    val x = parts[index + 1].toInt()
                    val y = parts[index + 3].toInt()
                    val id = if (parts.contains("npcid")) {
                        parts[parts.indexOf("npcid") + 1].toInt()
                    } else {
                        name.lowercase()
                    }
                    println("  { id = \"$id\", x = $x, y = $y${if (level != 0) ", level = $level" else ""}${if (members) ", members = true" else ""} },")
                }
                i++
                continue
            }
            val parts = line.removePrefix("|").split("=").map { it.trim() }
            when (parts[0]) {
                "location", "loc" -> println("  # ${parts[1].replace("[", "").replace("]", "")}")
                "name" -> name = parts[1]
                "members", "mem" -> members = parts[1].equals("yes", true) || parts[1].equals("true", true)
                "plane" -> level = parts[1].toInt()
            }
            i++
        }
    }
}
