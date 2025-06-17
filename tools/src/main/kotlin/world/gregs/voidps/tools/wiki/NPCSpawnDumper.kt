package world.gregs.voidps.tools.wiki

object NPCSpawnDumper {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = """
            |x:3198,y:3261|x:3198,y:3265|x:3198,y:3272|x:3204,y:3259|x:3207,y:3260|x:3202,y:3262|x:3205,y:3266|x:3193,y:3262|x:3197,y:3269|x:3204,y:3261|x:3210,y:3262|x:3202,y:3271|x:3196,y:3259|x:3195,y:3272|x:3210,y:3274|x:3194,y:3267
        """.trimIndent()
        input.replace("\n", "").removePrefix("|").removeSuffix("|").split("|").map {
            it.split(",").associate { str ->
                val split = str.split(":")
                split.first() to split.last()
            }
        }.forEach {
            val id = it["npcid"]?.toIntOrNull()
            val x = it.getValue("x").toInt()
            val y = it.getValue("y").toInt()
            val z = it["z"]?.toIntOrNull()
            println("- { ${if (id != null) "id: $id, " else ""}x: $x, y: $y${if (z != null) ", z: $z" else ""} }")
        }
    }
}
