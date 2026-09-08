package world.gregs.voidps.tools.convert

import java.io.File

/**
 * ```
 * statement(
 * "Removes dialogues which are formated"
 *  + "like this",
 * )
 * ```
 */
object DialogueLineBreakRemover {
    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("./game/src/main/kotlin/content/quest/member/myreque/")

        val files = when {
            file.isFile -> listOf(file)
            file.isDirectory -> file.walkTopDown().filter { it.isFile }.toList()
            else -> return
        }
        val start = Regex("((?:player|npc)<[a-zA-Z]+>|statement)\\([\\r\\n]\\s+\"")
        val middle = Regex("\"\\s\\+[\\r\\n]\\s+\"")
        val end = Regex("\",[\\r\\n]\\s+\\)")
        for (file in files) {
            var text = file.readText()

            text = replaceStart(start, text)
            text = text.replace(middle, "")
            text = text.replace(end, "\")")
            file.writeText(text)
        }
    }

    private fun replaceStart(start: Regex, text: String): String {
        val matches = mutableListOf<MatchResult>()
        for (result in start.findAll(text)) {
            matches.add(result)
        }
        var text = text
        for (match in matches.sortedByDescending { it.range.first }) {
            text = text.replaceRange(match.range, "${match.value.substringBefore('(')}(\"")
        }
        return text
    }
}