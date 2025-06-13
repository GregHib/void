package world.gregs.voidps.tools.wiki.dialogue

import world.gregs.voidps.tools.wiki.dialogue.Dialogue.Companion.NPC
import world.gregs.voidps.tools.wiki.dialogue.Dialogue.Companion.PLAYER
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration
import java.io.File
import java.util.*

/**
 * Parses dialogue yaml files into code to be queried
 * https://mega.nz/file/sJsRlDzI#E5U0ObIAz9CrAD2HWmJLaALdvrQyR2oaF2gcbWe5qVE
 */
@Suppress("UNCHECKED_CAST")
object DialogueParsing {

    private var printIds = false
    private var largeHeads = false

    @JvmStatic
    fun main(args: Array<String>) {
        println("Loading dialogue content...")
        val folder = File("./temp/chisel/dialogue/")
        val content = content(folder)
        while (true) {
            println("Enter a starting dialogue id or npc name:")
            val input = readln().trim()
            when (input) {
                "ids", "print ids", "printIds", "debug" -> {
                    printIds = !printIds
                    println("printIds = $printIds")
                }
                "exit", "quit" -> {
                    break
                }
            }
            val id = input.toIntOrNull()
            if (id != null) {
                println("```kotlin")
                printChain(id, content)
                println("```")
            } else {
                val valid = content.values.filter { it.type == NPC && it.name.equals(input, ignoreCase = true) }
                if (valid.isEmpty()) {
                    println("Unable to find any dialogues for npc name '$input'.")
                    continue
                }
                println("Dialogue id = Dialogue text")
                println("---------------------------")
                for (dialogue in valid) {
                    println("${dialogue.id} = ${dialogue.text}")
                }
                println("---------------------------")
            }
        }
    }

    private fun printChain(originId: Int, content: Map<Int, Dialogue>) {
        val printed = mutableSetOf<Int>()
        queue.add(originId to false)
        var npcName = ""
        while (queue.isNotEmpty()) {
            val (id, skip) = queue.poll()
            val dialogue = content[id] ?: continue
            if (!printed.add(id)) {
                continue
            }
            if (npcName.isBlank() && dialogue.type == "DIALOGUE_NPC") {
                npcName = dialogue.name
            }
            println("\nsuspend fun CharacterContext<Player>.${dialogue.methodName()} {")
            val options = print(content, id, name = npcName, skip = skip)
            println("}")
            if (options != null && printed.add(options.id)) {
                printOptions(content, options, printIds, npcName)
            }
        }
    }

    private val queue = LinkedList<Pair<Int, Boolean>>()

    private fun print(content: Map<Int, Dialogue>, id: Int, depth: Int = 0, name: String, skip: Boolean = false): Dialogue? {
        val dialogue = content[id] ?: return null
        if (dialogue.text.contains(':') || depth > 10) {
            return null
        }
        val withId = if (printIds) " // ${dialogue.id}" else ""
        when (dialogue.type) {
            "DIALOGUE_OPTIONS" -> {
                val choiceMethod = dialogue.methodName()
                println("    $choiceMethod")
                return dialogue
            }
            "DIALOGUE_NPC", "DIALOGUE_PLAYER" -> {
                if (dialogue.type == "DIALOGUE_NPC") {
                    println("    npc<Talk>(\"${dialogue.text}\"${if (largeHeads) ", largeHead = true" else ""})$withId")
                } else if (!skip) {
                    println("    player<Talk>(\"${dialogue.text}\")$withId")
                }
                if (dialogue.next.isNotEmpty()) {
                    val next = content[dialogue.next.first()]
                    val count = dialogue.nextCount.first()
                    if (next != null && isValidNextDialogue(dialogue, next, count, name)) {
                        return print(content, next.id, depth + 1, name)
                    }
                }
            }
        }
        return null
    }

    private fun printOptions(content: Map<Int, Dialogue>, dialogue: Dialogue, printIds: Boolean, name: String) {
        println("\nsuspend fun CharacterContext<Player>.${dialogue.methodName()} {")
        println("    choice(\"${dialogue.options.first()}\") {${if (printIds) " // ${dialogue.id}" else ""}")
        for (option in dialogue.options.drop(1)) {
            val match =
                dialogue.next.firstOrNull { content[it]?.text == option && (content[it]?.name == dialogue.name || content[it]?.name == "Player") } // ?: content.values.firstOrNull { it.text == option }?.id
            if (match != null) {
                val next = content[content[match]!!.next.first()]
                if (next != null && isValidNextDialogue(dialogue, next, Int.MAX_VALUE, name)) {
                    println("        option<Talk>(\"${option}\") {")
                    queue.add(match to true)
                    println("            ${content[match]!!.methodName()}")
                    println("        }")
                } else {
                    println("        option<Talk>(\"${option}\")")
                }
            } else {
                println("        option(\"${option}\")")
            }
        }
        println("    }")
        println("}")
    }

    private fun isValidNextDialogue(current: Dialogue, next: Dialogue, count: Int, name: String): Boolean {
        if (next.type == NPC && next.name != name) {
            return false
        }
        if (current.type == PLAYER && next.type == PLAYER) {
            return false
        }
        return count > 50
    }

    private fun content(folder: File): Map<Int, Dialogue> {
        val yaml = Yaml()
        val config = object : YamlReaderConfiguration() {
            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                super.set(map, key, if (indent == 0) Dialogue.fromMap(value as Map<String, Any>, key) else value, indent, parentMap)
            }
        }
        return (yaml.read(folder.resolve("content.yaml").readText(), config) as Map<String, Dialogue>).mapKeys { it.key.toInt() }
    }

    private fun npcs(folder: File): Map<String, List<Int>> {
        val yaml = Yaml()
        return yaml.read(folder.resolve("npcs.yaml").readText()) as Map<String, List<Int>>
    }
}
