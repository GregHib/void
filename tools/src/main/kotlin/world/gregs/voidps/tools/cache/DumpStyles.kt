package world.gregs.voidps.tools.cache

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.cache.definition.data.Instructions
import world.gregs.voidps.cache.definition.decoder.ClientScriptDecoder
import world.gregs.voidps.engine.data.config.WeaponStyleDefinition
import world.gregs.voidps.tools.property
import java.io.File

object DumpStyles {
    private val names = arrayOf("unarmed",
        "staff",
        "axe",
        "sceptre",
        "pickaxe",
        "dagger",
        "sword",
        "2h",
        "mace",
        "claws",
        "hammer",
        "whip",
        "fun",
        "pie",
        "spear",
        "halberd",
        "bow",
        "crossbow",
        "thrown",
        "chinchompa",
        "fixed_device",
        "salamander",
        "scythe",
        "flail",
        "sling",
        "trident",
        "staff_of_light")

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val decoder = ClientScriptDecoder().load(cache)
        val clientScript = decoder[1142]
        load(clientScript)
        val builder = StringBuilder()
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            builder.append(def.toString()).append("\n")
        }
        File("enums.txt").writeText(builder.toString())
    }

    fun load(script: ClientScriptDefinition) {
        val switchIndex = script.instructions.indexOf(Instructions.SWITCH)
        val indices = script.switchStatementIndices ?: return
        val ints = script.intOperands ?: return
        val strings = script.stringOperands ?: return
        var highest = 0
        val list = mutableListOf<WeaponStyleDefinition>()
        list.add(WeaponStyleDefinition.EMPTY)
        for ((key, offset) in indices[ints[switchIndex]]) {
            val index = switchIndex + offset
            getStyles(key, index, script.instructions, strings)
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
        getStyles(0, default, script.instructions, strings)
    }

    private fun getStyles(id: Int, index: Int, instructions: IntArray, strings: Array<String?>) {
        val types = mutableListOf<String>()
        val styles = mutableListOf<String>()
        val combatStyles = mutableListOf<String>()
        var index: Int = index
        var last = -1
        while (index <= instructions.lastIndex) {
            val instruction = instructions[index]
            when (instruction) {
                Instructions.PUSH_STRING -> strings[index]?.also { string ->
                    if (last == Instructions.GOTO || last == Instructions.MERGE_STRINGS) {
                        if (string.isNotEmpty())
                            types.add(string.toSnakeCase())
                    } else if (last == Instructions.PUSH_INT) {
                        if (string.isNotEmpty())
                            styles.add(string.toSnakeCase())
                    } else if (last == Instructions.PUSH_STRING && strings[index - 1] == "<br>" && !string.endsWith("XP")) {
                        if (string.isNotEmpty())
                            combatStyles.add(string.toSnakeCase())
                    }
                }
                Instructions.CALL_CS2 -> break
            }
            last = instruction
            index++
        }
        println("""
            ${names[id]}:
              id: $id
              attack_types: $types
              attack_styles: $styles
              combat_styles: $combatStyles
        """.trimIndent())
//        return types.mapIndexed { i, s -> Triple(s, styles[i], combatStyle.getOrNull(i) ?: "") }.toTypedArray()
    }
}