package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.cache.definition.data.Instructions
import world.gregs.voidps.cache.definition.decoder.ClientScriptDecoder
import world.gregs.voidps.engine.timedLoad

class StyleDefinitions {

    private lateinit var definitions: Map<Int, Array<Triple<String, String, String>>>

    fun get(type: Int): Array<Triple<String, String, String>>? = definitions[type]

    fun contains(key: Int) = definitions.containsKey(key)

    fun load(decoder: ClientScriptDecoder): StyleDefinitions {
        timedLoad("style definition") {
            load(decoder.get(1142))
        }
        return this
    }

    fun load(script: ClientScriptDefinition): Int {
        val switchIndex = script.instructions.indexOf(Instructions.SWITCH)
        val indices = script.switchStatementIndices ?: return 0
        val ints = script.intOperands ?: return 0
        val strings = script.stringOperands ?: return 0
        var highest = 0
        val map = mutableMapOf<Int, Array<Triple<String, String, String>>>()
        for ((key, offset) in indices[ints[switchIndex]]) {
            val index = switchIndex + offset
            map[key] = getStyles(index, script.instructions, strings)
            if (index > highest) {
                highest = index
            }
        }
        var default = highest
        for (index in highest + 1 until script.instructions.lastIndex) {
            if (script.instructions[index] == Instructions.CALL_CS2) {
                default = index + 1
                break
            }
        }
        map[0] = getStyles(default, script.instructions, strings)
        definitions = Int2ObjectOpenHashMap(map)
        return definitions.size
    }

    private fun getStyles(index: Int, instructions: IntArray, strings: Array<String?>): Array<Triple<String, String, String>> {
        val types = mutableListOf<String>()
        val styles = mutableListOf<String>()
        val combatStyle = mutableListOf<String>()
        var index: Int = index
        var last = -1
        while (index <= instructions.lastIndex) {
            val instruction = instructions[index]
            when (instruction) {
                Instructions.PUSH_STRING -> strings[index]?.also { string ->
                    if (last == Instructions.GOTO || last == Instructions.MERGE_STRINGS) {
                        types.add(string.toSnakeCase())
                    } else if (last == Instructions.PUSH_INT) {
                        styles.add(string.toSnakeCase())
                    } else if (last == Instructions.PUSH_STRING && strings[index - 1] == "<br>" && !string.endsWith("XP")) {
                        combatStyle.add(string.toSnakeCase())
                    }
                }
                Instructions.CALL_CS2 -> break
            }
            last = instruction
            index++
        }
        return types.mapIndexed { i, s -> Triple(s, styles[i], combatStyle.getOrNull(i) ?: "") }.toTypedArray()
    }
}