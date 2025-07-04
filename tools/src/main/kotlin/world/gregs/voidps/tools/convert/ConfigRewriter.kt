package world.gregs.voidps.tools.convert

import java.io.File

/**
 * Rewrites file(s) with amendments
 */
object ConfigRewriter {
    private val excluded = setOf(
        "saradomin_flag",
        "saradomin_cloak_castle_wars",
        "saradomin_team_hood",
        "saradomin_team_cape",
        "zamorak_flag",
        "zamorak_cloak_castle_wars",
        "zamorak_team_hood",
        "zamorak_team_cape"
    )

    private val included = mapOf(
        "ancient_mace" to "bandos",
        "granite_mace" to "bandos",
        "unholy_symbol" to "zamorak",
        "holy_symbol" to "saradomin",
        "ring_of_devotion" to "saradomin",
        "staff_of_light" to "saradomin",
        "monks_robe_bottom" to "saradomin",
        "monks_robe_top" to "saradomin",
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val files = File("./data/").walkTopDown().filter { it.isFile && it.name.endsWith("items.toml") }

        for (file in files) {
            val lines = file.readLines()
            val output = StringBuilder()
            var block = ""
            var different = false
            for (i in lines.indices) {
                val line = lines[i]
                if (line.startsWith('[')) {
                    block = line.substringBefore(']').removePrefix("[")
                }
                if (line.startsWith("slot = ")) {
                    if (included.containsKey(block)) {
                        output.appendLine("god = \"${included[block]}\"")
                        different = true
                    } else if (block.contains("saradomin")) {
                        if (!excluded.contains(block)) {
                            output.appendLine("god = \"saradomin\"")
                            different = true
                        }
                    } else if (block.contains("zamorak") || block.contains("dagonhai")) {
                        if (!excluded.contains(block)) {
                            output.appendLine("god = \"zamorak\"")
                            different = true
                        }
                    } else if (block.contains("bandos")) {
                        output.appendLine("god = \"bandos\"")
                        different = true
                    } else if (block.contains("armadyl")) {
                        output.appendLine("god = \"armadyl\"")
                        different = true
                    } else if (block.contains("zaryte") || block.contains("torva") || block.contains("pernix") || block.contains("virtus")) {
                        output.appendLine("god = \"zaros\"")
                        different = true
                    }
                }
                output.appendLine(line)
            }
            if (different) {
                file.writeText(output.toString())
            }
        }
    }
}