package world.gregs.voidps.tools.wiki.dialogue

import world.gregs.voidps.tools.wiki.dialogue.Dialogue.Companion.NPC
import world.gregs.voidps.tools.wiki.dialogue.Dialogue.Companion.OPTIONS
import world.gregs.voidps.tools.wiki.dialogue.Dialogue.Companion.PLAYER
import world.gregs.voidps.type.Distance
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration
import java.io.File
import java.util.*
import kotlin.math.max

/**
 * Parses dialogue yaml files into code
 */
object DialogueParsing {

    internal data class Node(val id: Int, val type: String, val name: String, var depth: Int = 0) {
        val options = mutableMapOf<Int, MutableSet<Node>>()
        val parents = mutableSetOf<Node>()

        val next = mutableListOf<Node>()
        val previous = mutableListOf<Node>()
        var visited = false

        var group = Int.MAX_VALUE

        fun group(group: Int) {
            if (group < this.group) {
                this.group = group
            }
        }

        fun option(index: Int): MutableSet<Node> {
            return options.getOrPut(index) { mutableSetOf() }
        }

        fun depth(depth: Int) {
            if (depth > this.depth) {
                return
            }
            this.depth = depth
        }

        var lastUpdate = Int.MAX_VALUE

        fun childDepth(depth: Int) {
            if (depth >= lastUpdate) {
                return
            }
            lastUpdate = depth
            for (child in next) {
                child.depth(depth)
            }
            for (child in previous) {
                child.depth(depth)
            }
        }

        constructor(dialogue: Dialogue, distance: Int = 0) : this(dialogue.id, dialogue.type, dialogue.name, distance)
    }

    fun toType(type: String) = when (type) {
        "DIALOGUE_PLAYER" -> 0
        "DIALOGUE_NPC" -> 1
        "DIALOGUE_OPTIONS" -> 2
        else -> -1
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val folder = File("./temp/chisel/dialogue/")
        val name = "Guardian mummy"
        val content = content(folder)


        // Create graph
        val nodes = mutableMapOf<Int, Node>()
        for (dialogue in content.values) {
            nodes[dialogue.id] = Node(dialogue.id, dialogue.type, dialogue.name, if (dialogue.name == name) 0 else Int.MAX_VALUE)
        }
        for (dialogue in content.values) {
            val node = nodes[dialogue.id] ?: continue
            for (id in dialogue.next) {
                val child = nodes[id] ?: continue
                if (child.type == NPC && child.name != name) {
                    continue
                }
                node.next.add(child)
            }
            for (id in dialogue.previous) {
                val child = nodes[id] ?: continue
                if (child.type == NPC && child.name != name) {
                    continue
                }
                node.previous.add(child)
            }
        }

        // Search graph
        val queue = PriorityQueue<Node> { o1, o2 -> o1.depth.compareTo(o2.depth) }
        val originals = nodes.values.filter { it.depth == 0 }
        originals.forEachIndexed { index, node -> node.group(index) }
        queue.addAll(originals)
        while (queue.isNotEmpty()) {
            val node = queue.poll()
            if (node.depth > 2) {
                continue
            }
            node.childDepth(node.depth + 1)
            if (node.visited) {
                continue
            }
            node.visited = true
            when (node.type) {
                NPC, PLAYER -> {
                    val next = node.next.firstOrNull() ?: continue
                    val children = node.next.drop(1)
                    for (child in children) {
                        child.parents.remove(node)
                    }
                    node.next.removeAll(children)
                    if (node.type == PLAYER && next.type == PLAYER) {
                        next.parents.remove(node)
                        node.next.remove(node)
                        continue
                    }
                    if (next.type == NPC && node.name != name) {
                        next.parents.remove(node)
                        node.next.remove(node)
                        continue
                    }
                    queue.add(next)
                }
                OPTIONS -> {
                    val dialogue = content[node.id] ?: continue
                    val options = mutableMapOf<Int, Node>()
                    for ((index, option) in dialogue.options.drop(1).withIndex()) {
                        options[index] = node.next
                            .firstOrNull { it.type == PLAYER && content[it.id]?.text == option }
                            ?: continue
                    }

                    val dropped = node.next.dropWhile { options.values.contains(it) }
                    for (drop in dropped) {
                        drop.parents.remove(node)
                    }
                    queue.addAll(options.values)
                }
            }
            val it = node.previous.iterator()
            while (it.hasNext()) {
                val child = it.next()
                if (child.type == OPTIONS && (node.type != PLAYER || content[child.id]?.options?.contains(content[node.id]?.text) != true)) {
                    child.next.remove(node)
                    it.remove()
                    continue
                }
                if (child.type == PLAYER && node.type == PLAYER) {
                    child.next.remove(node)
                    it.remove()
                    continue
                }
                if (child.type == NPC && child.name != name) {
                    child.next.remove(node)
                    it.remove()
                    continue
                }
                queue.add(child)
            }
        }

        val depth = nodes.values.filter { it.visited }.sortedBy { it.depth }
        println("Graph: ${nodes.size}")
        println("Visited: ${nodes.values.filter { it.visited }.size}")

        val groups = depth.groupBy { it.group }
        println("Groups: ${groups.keys}")


//        for ((_, nodes) in groups) {
//            println("Size: ${nodes.filter { it.depth < 1 }.size}")
//            println(nodes.filter { it.depth <= 1 })
//        }

        /*
            TODO
              * check if linking is correct & any more filtering can be done
              * how to traverse / find the chain
                start with options that are depth 1 into methods and then would out from there until all npc chats are covered?
         */

    }

    private fun effort(content: Map<Int, Dialogue>, name: String) {
        val ids = content.mapValues { it.value.next }
        val nodes = mutableMapOf<Int, Node>()
        val queue = LinkedList<Pair<Node, Int>>()
        val startNodes = content.values.filter { it.name == name }.map { nodes.getOrPut(it.id) { Node(it) } to 0 }
        queue.addAll(startNodes)
        val frontier = mutableSetOf<Int>()
        while (queue.isNotEmpty()) {
            val (node, depth) = queue.poll()
            if (!frontier.add(node.id) || depth < -2) {
                continue
            }
            val dialogue = content[node.id] ?: continue
            println("Visit ${node.id} ${dialogue.dialogue}")

            when (node.type) {
                OPTIONS -> {
                    for ((index, option) in dialogue.options.drop(1).withIndex()) {
                        val id = dialogue.next.firstOrNull { content[it]!!.text == option }
                            ?: dialogue.next.sortedByDescending { differencePercent(option, content[it]!!.text) }
                                .firstOrNull { dif -> dif < 50.0 }
                        if (id == null) {
                            val emptyNode = nodes.getOrPut(-1) { Node(-1, "", "") }
                            emptyNode.parents.add(node)
                            node.option(index).add(emptyNode)
                            continue
                        }
                        queueNode(content, queue, nodes, id, name, node, index, depth)
                    }
                }
                PLAYER, NPC -> {
                    val id = ids[node.id]?.firstOrNull() ?: continue
                    queueNode(content, queue, nodes, id, name, node, 0, depth)
                }
            }

            for (id in dialogue.previous) {
                val parent = content[id] ?: continue
                if (!parent.next.contains(dialogue.id) || parent.text.isBlank()) {
                    continue
                }
                if (parent.type == OPTIONS && (dialogue.type != PLAYER || dialogue.text.isBlank() || !parent.options.drop(1).contains(dialogue.text))) {
                    continue
                }

                if (parent.type == PLAYER && dialogue.type == PLAYER) {
                    continue
                }

                if (parent.type == NPC && parent.name != name) {
                    continue
                }

                val n = nodes.getOrPut(id) { Node(parent) }
                queue.add(n to depth - 1)
//                n.depth = max(n.depth, node.depth + 1)
            }

        }

//            longestPath(content, start, 0, results = results)


        for (start in nodes.values) {
            val methodsQueue = LinkedList<Pair<String, Node>>()
            val visited = mutableSetOf<Int>()
            val methods = mutableSetOf<String>()
            methodsQueue.add("main()" to start)
            while (methodsQueue.isNotEmpty()) {
                val (name, node) = methodsQueue.poll()
                if (!methods.add(name)) {
                    continue
                }
                println("fun $name {")
                print(methodsQueue, content, node, depth = 1, visits = visited)
                println("}")
            }
        }
        println()
        println()
        println()
        println()
//        val visits = mutableSetOf<Int>()
//        for (node in nodes.values) {
//            if (traverse(content, node, visits = visits)) {
//                println()
//            }
//        }
    }

    const val TAB = "    "
    fun idt(depth: Int) = TAB.repeat(depth)

    private fun print(queue: Queue<Pair<String, Node>>, content: Map<Int, Dialogue>, node: Node, depth: Int = 0, visits: MutableSet<Int> = mutableSetOf()): Boolean {
        if (!visits.add(node.id)) {
            return false
        }
        val dialogue = content[node.id] ?: return false
        when (node.type) {
            OPTIONS -> {
                if (depth == 1) {
                    for ((index, option) in dialogue.options.withIndex()) {
                        if (index == 0) {
                            println("${TAB}choice(\"$option\") {")
                        } else {
                            println("${idt(2)}option(\"$option\") {")
                            println("${idt(3)}${Dialogue.toMethodName(option)}")
                            println("${idt(2)}}")
                        }
                    }
                    println("${TAB}}")
                    for ((index, candidates) in node.options) {
                        val methodName = Dialogue.toMethodName(dialogue.options[index])
                        for (child in candidates) {
                            queue.add(methodName to child)
                        }
                    }
                } else {
                    println("${idt(depth)}${dialogue.methodName()}")
                }
            }
            else -> {
                println("${idt(depth)}${if (node.type == NPC) "npc" else "player"}<Talk>(\"${dialogue.text}\")")
                for ((_, candidates) in node.options) {
                    for (child in candidates) {
                        print(queue, content, child, depth, visits)
                    }
                }
            }
        }
        return true
    }

    private fun traverse(content: Map<Int, Dialogue>, node: Node, depth: Int = 0, visits: MutableSet<Int> = mutableSetOf()): Boolean {
        if (!visits.add(node.id)) {
            return false
        }
        val dialogue = content[node.id] ?: return false
        when (node.type) {
            OPTIONS -> {
                println("${" ".repeat(depth)}${Dialogue.toMethodName(dialogue.options.first())}")
//                for ((_, candidates) in node.options) {
//                    for (child in candidates) {
//                        traverse(content, child, depth, visits)
//                    }
//                }
            }
            else -> {
                println("${" ".repeat(depth)}${if (node.type == NPC) "npc" else "player"}<Talk>(${dialogue.text})")
                for ((_, candidates) in node.options) {
                    for (child in candidates) {
                        traverse(content, child, depth, visits)
                    }
                }
            }
        }
        return true
    }

    private fun differencePercent(second: String, first: String) =
        Distance.levenshtein(first, second) / max(first.length, second.length).toDouble() * 100.0

    private fun queueNode(content: Map<Int, Dialogue>, queue: Queue<Pair<Node, Int>>, nodes: MutableMap<Int, Node>, id: Int?, name: String, parent: Node, index: Int, depth: Int) {
        val dialogue = content[id] ?: return
        if (dialogue.type == NPC && dialogue.name != name || id == parent.id || (parent.type == OPTIONS && dialogue.type != PLAYER)) {
            return
        }
        val child = nodes.getOrPut(dialogue.id) { Node(dialogue) }
        child.parents.add(parent)
//        child.depth = max(parent.depth, child.depth + 1)
        parent.option(index).add(child)
        queue.add(child to depth + 1)
    }

    private fun longestPath(content: Map<Int, Dialogue>, node: Node, depth: Int = 0, visits: MutableSet<Int> = mutableSetOf(), results: MutableSet<Pair<Node, Int>>) {
        if (!visits.add(node.id)) {
            return
        }
        val dialogue = content[node.id] ?: return
        println("${" ".repeat(depth)}${node.id} - ${dialogue.dialogue}")
        for (parent in node.parents) {
            longestPath(content, parent, depth + 1, visits, results)
        }
        if (node.parents.isEmpty()) {
            results.add(node to depth)
        }
    }

    private fun printChain(folder: File, id: Int, printIds: Boolean) {
        val yaml = Yaml()
        val config = object : YamlReaderConfiguration() {
            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                super.set(map, key, if (indent == 0) Dialogue.fromMap(value as Map<String, Any>, key) else value, indent, parentMap)
            }
        }
        val content = (yaml.read(folder.resolve("content.yaml").readText(), config) as Map<String, Dialogue>).mapKeys { it.key.toInt() }
        val printed = mutableSetOf<Int>()
        queue.add(id to false)
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
            println("fun ${dialogue.methodName()} {")
            val options = print(content, id, indent = "    ", printId = printIds, name = npcName, skip = skip)
            println("}")
            if (options != null) {
                printOptions(content, options, printIds, npcName)
            }
        }
    }

    private val queue = LinkedList<Pair<Int, Boolean>>()

    private fun print(content: Map<Int, Dialogue>, id: Int, indent: String = "", depth: Int = 0, printId: Boolean = true, name: String, skip: Boolean = false): Dialogue? {
        val dialogue = content[id] ?: return null
        if (dialogue.text.contains(':') || depth > 10) {
            return null
        }
        val withId = if (printId) " // ${dialogue.id}" else ""
        when (dialogue.type) {
            "DIALOGUE_OPTIONS" -> {
                val choiceMethod = Dialogue.toMethodName(dialogue.options.first())
                println("${indent}$choiceMethod")
                return dialogue
            }
            "DIALOGUE_NPC" -> {
                println("${indent}npc<Talk>(\"${dialogue.text}\")$withId")
                if (dialogue.next.isNotEmpty()) {
                    val next = content[dialogue.next.first()]
                    if (next != null && isValidNextDialogue(dialogue, next, name)) {
                        return print(content, next.id, indent, depth + 1, printId, name)
                    }
                }
            }
            "DIALOGUE_PLAYER" -> {
                if (!skip) {
                    println("${indent}player<Talk>(\"${dialogue.text}\")$withId")
                }
                if (dialogue.next.isNotEmpty()) {
                    val next = content[dialogue.next.first()]
                    if (next != null && isValidNextDialogue(dialogue, next, name)) {
                        return print(content, next.id, indent, depth + 1, printId, name)
                    }
                }
            }
        }
        return null
    }

    private fun printOptions(content: Map<Int, Dialogue>, dialogue: Dialogue, printIds: Boolean, name: String) {
        println("fun ${Dialogue.toMethodName(dialogue.options.first())} {")
        println("    choice(\"${dialogue.options.first()}\") {${if (printIds) " // ${dialogue.id}" else ""}")
        for (option in dialogue.options.drop(1)) {
            val match =
                dialogue.next.firstOrNull { content[it]?.text == option && (content[it]?.name == dialogue.name || content[it]?.name == "Player") }// ?: content.values.firstOrNull { it.text == option }?.id
            if (match != null) {
                val next = content[content[match]!!.next.first()]
                if (isValidNextDialogue(dialogue, next, name)) {
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

    private fun isValidNextDialogue(current: Dialogue, next: Dialogue?, name: String) = next != null && (/*next.previous == current.id ||*/ next.name == name)


    private fun printDialoguesForNpc(folder: File, name: String) {
        val yaml = Yaml()
        val config = object : YamlReaderConfiguration() {
            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                super.set(map, key, if (indent == 0) Dialogue.fromMap(value as Map<String, Any>, key) else value, indent, parentMap)
            }
        }
        val content = (yaml.read(folder.resolve("content.yaml").readText(), config) as Map<String, Dialogue>).mapKeys { it.key.toInt() }
        for ((_, dialogue) in content) {
            if (dialogue.name.equals(name, true)) {
                dialogue.print()
//                println("${dialogue.id} ${content[dialogue.previous]?.dialogue} -> ${dialogue.dialogue} -> ${dialogue.next.map { content[it]?.dialogue }}")
            }
        }
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