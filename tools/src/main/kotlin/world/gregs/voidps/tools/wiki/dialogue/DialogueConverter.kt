package world.gregs.voidps.tools.wiki.dialogue

import com.google.common.base.CaseFormat
import com.google.common.base.CharMatcher
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.pearx.kasechange.toPascalCase
import net.pearx.kasechange.toSnakeCase
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration
import java.io.File
import java.util.*

/**
 * Converts raw data from runelites data collection project into dialogue scripts
 */
@Suppress("UNCHECKED_CAST")
object DialogueConverter {

    data class DialogueOption(
        val child: DialogueUnion? = null,
        val message: String? = null,
    ) {
        companion object {

            operator fun invoke(map: Map<String, Any>): DialogueOption = DialogueOption(
                map["child"] as? DialogueUnion,
                map["message"] as? String,
            )
        }
    }

    data class DialogueUnion(
        var text: String?,
        var name: String?,
        var npc: Int?,
        var animation: Int?,
        var neighbors: List<DialogueUnion>? = null,
        var options: List<DialogueOption>? = null,
    ) {

        val builder: StringBuilder by lazy { StringBuilder() }

        override fun hashCode(): Int = if (isRootNode) {
            Objects.hash(neighbors)
        } else {
            Objects.hash(npc, text, name, options)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }
            val (text1, name1, npc1, _, neighbors1, options1) = other as DialogueUnion
            return if (isRootNode) {
                if (neighbors == neighbors1) {
                    return !(neighbors != null && neighbors1 != null && neighbors1.containsAll(neighbors!!))
                }
                false
            } else {
                npc == npc1 && text == text1 && name == name1 && options == options1
            }
        }

        val isNPCDialogue: Boolean
            get() = npc != null && animation != null && text != null && name != null
        val isOptionsDialogue: Boolean
            get() = options != null
        val isPlayerDialogue: Boolean
            get() = animation != null && text != null
        val isMessageDialogue: Boolean
            get() = text != null && animation == null && npc == null && options == null
        val isRootNode: Boolean
            get() = npc == null && animation == null && text == null && name == null && neighbors != null

        companion object {

            operator fun invoke(map: Map<String, Any>): DialogueUnion = DialogueUnion(
                map["text"] as? String,
                map["name"] as? String,
                map["npc"] as? Int,
                map["animation"] as? Int,
                map["neighbors"] as? List<DialogueUnion>,
                map["options"] as? List<DialogueOption>,
            )
        }
    }

    private fun getParents(node: DialogueUnion): ObjectOpenHashSet<DialogueUnion> {
        val queue = ArrayDeque<DialogueUnion>()
        val parentQueue = ArrayDeque<DialogueUnion>()
        val branchToFunction = Object2ObjectOpenHashMap<DialogueUnion?, String>()
        val parents = ObjectOpenHashSet<DialogueUnion>()
        queue.push(node)
        parentQueue.push(node)
        branchToFunction[node] = "startDialogue"
        var previousParent = node
        while (!queue.isEmpty()) {
            val front = queue.poll()
            val parent = parentQueue.poll()
            printMembers(node.npc!!, previousParent, parent, parentQueue.peekFirst(), front, branchToFunction, parents)
            previousParent = parent
            val neighbors = front.neighbors ?: continue
            for (neighbor in neighbors) {
                queue.addLast(neighbor)
                parentQueue.addLast(front)
                val options = neighbor.options ?: continue
                for (option in options) {
                    val child = option.child ?: continue
                    queue.addLast(child)
                    parentQueue.addLast(neighbor)
                }
            }
        }
        return parents
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val input = File("${System.getProperty("user.home")}/Downloads/complete-dialogues/")
        val yaml = Yaml()
        val reader = object : YamlReaderConfiguration() {
            override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                if (value is Map<*, *>) {
                    value as Map<String, Any>
                    super.add(list, if (value.containsKey("child")) DialogueOption(value) else DialogueUnion(value), parentMap)
                } else {
                    super.add(list, value, parentMap)
                }
            }

            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                if (value is Map<*, *>) {
                    value as Map<String, Any>
                    super.set(map, key, if (value.containsKey("child")) DialogueOption(value) else DialogueUnion(value), indent, parentMap)
                } else {
                    super.set(map, key, value, indent, parentMap)
                }
            }
        }
        val outputPath = File("./temp/dialogue-scripts/")
        outputPath.mkdirs()
        input.walkTopDown().forEach { file ->
            if (!file.isFile || file.extension != "json") {
                return@forEach
            }
            val index = file.nameWithoutExtension.indexOfLast { it == '-' }
            val name = file.nameWithoutExtension.substring(0, index)
            val relative = file.parent.replace(input.path, "").removePrefix("\\")
            val text = file.readText().replace("\" + player.getName() + \"", "{\$player_name}").replace("{\$player_name}", "<player_name>")
            val root = DialogueUnion(yaml.read(text, reader) as Map<String, Any>)
            val out = outputPath.resolve(relative).resolve("${name.replace("'", "").toPascalCase()}.kts")
            out.parentFile.mkdirs()
            writeAll(out, name.toSnakeCase(), getParents(root))
        }
    }

    private fun getNpcName(id: Int): String = "$id"

    private fun writeAll(file: File, name: String, functions: ObjectOpenHashSet<DialogueUnion>) {
        val sorted = functions.sortedBy { it.neighbors?.size ?: 0 }.toMutableList()
        val existing = ObjectOpenHashSet<String>()
        sorted.removeIf { parent: DialogueUnion ->
            val builder: StringBuilder = parent.builder
            val functionName: String = builder.substring(0, builder.indexOf("\n"))
            if (!existing.contains(functionName)) {
                existing.add(functionName)
                return@removeIf false
            }
            true
        }

        if (file.exists()) {
            file.delete()
        }
        file.appendText(
            """
            package world.gregs.voidps.world.map

            import world.gregs.voidps.engine.entity.character.player.Player
            import content.entity.player.dialogue.*
            import content.entity.player.dialogue.type.*
            import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
            import world.gregs.voidps.engine.entity.character.npc.NPC
            import world.gregs.voidps.engine.entity.character.npc.npcOperate
            
            npcOperate("Talk-to", "$name") {
                startDialogue()
            }
            """.trimIndent(),
        )
        file.appendText("\n\n")
        for (parent: DialogueUnion in sorted) {
            file.appendText(parent.builder.toString().replace("\t", "    "))
        }
    }

    private fun printMembers(
        npc: Int,
        previousParent: DialogueUnion,
        parent: DialogueUnion,
        nextParent: DialogueUnion?,
        front: DialogueUnion,
        branchToFunction: Object2ObjectOpenHashMap<DialogueUnion?, String>,
        parents: ObjectOpenHashSet<DialogueUnion>,
    ) {
        val lastNode = parent != nextParent
        val builder = parent.builder
        if (parent != previousParent) {
            val function = branchToFunction[parent]
            if (function != null) {
                builder.append("suspend fun TargetInteraction<Player, NPC>.").append(function).append("() {\n")
            }
        }
        if (front.isNPCDialogue) {
            val anim = getAnimName(front.animation)
            builder.append("\tnpc").append("<").append(anim).append(">").append("(")
            if (npc != front.npc) {
                builder.append(getNpcName(front.npc!!)).append(", ")
            }
            builder.append("\"").append(front.text).append("\"").append(")")
        } else if (front.isPlayerDialogue) {
            val anim = getAnimName(front.animation)
            builder.append("\tplayer").append("<").append(anim).append(">").append("(").append("\"").append(front.text).append("\"").append(")")
        } else if (front.isMessageDialogue) {
            builder.append("\tmessage").append("(").append("\"").append(front.text).append("\")")
        } else if (front.isOptionsDialogue) {
            val options = front.options
//            branchToFunction[front] = toFunction(options?.first()?.message)
            builder.append("\tchoice {\n")
            var count = 0
            for (option: DialogueOption in options!!) {
                count++
                val function = toFunction(option.message)
                branchToFunction[option.child] = function
                builder.append("\t\toption(\"").append(option.message).append("\") {\n\t\t\t").append(function).append("()\n\t\t}")
                if (count == options.size) {
                    continue
                }
                builder.append("\n")
            }
            builder.append("\n\t}")
        } else {
            val function = branchToFunction[parent]
            if (function != null) {
                builder.append("suspend fun TargetInteraction<Player, NPC>.").append(function).append("() {\n")
            }
            return
        }
        if (!lastNode) {
            builder.append("\n")
        } else {
            builder.append("\n}\n\n")
            parents.add(parent)
        }
    }

    private fun getAnimName(animation: Int?): String = when (animation) {
        554, 555, 556, 557 -> "Quiz"
        562, 563, 564, 565 -> "Bored"
        567, 568, 569, 570 -> "Happy"
        571, 572, 573, 574 -> "Shock"
        575, 576, 577, 578 -> "Confused"
        588, 589, 590, 591 -> "Neutral"
        592, 593, 594, 595 -> "Shifty"
        596, 597, 598, 599 -> "Scared"
        600, 601, 602, 603 -> "Drunk"
        605, 606, 607, 608 -> "Laugh"
        609 -> "EvilLaugh"
        610, 611, 612, 613 -> "Sad"
        614, 615, 616, 617 -> "Angry"
        else -> animation.toString()
    }

    private fun toFunction(message: String?): String {
        val t = CharMatcher.javaLetterOrDigit().or(CharMatcher.whitespace()).retainFrom(message).replace("\\s".toRegex(), "_").uppercase(Locale.getDefault())
        return CaseFormat.UPPER_UNDERSCORE
            .to(CaseFormat.LOWER_CAMEL, t)
    }
}
