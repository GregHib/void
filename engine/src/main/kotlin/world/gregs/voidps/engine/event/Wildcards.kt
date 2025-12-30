package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.types.NPCTypes
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import java.io.File

object Wildcards {
    private val npcs = Object2ObjectOpenHashMap<String, List<String>>(128)
    private val objects = Object2ObjectOpenHashMap<String, List<String>>(64)
    private val interfaces = Object2ObjectOpenHashMap<String, List<String>>(0)
    private val components = Object2ObjectOpenHashMap<String, List<String>>(32)
    private val items = Object2ObjectOpenHashMap<String, List<String>>(32)
    private val variables = Object2ObjectOpenHashMap<String, List<String>>(2)
    private val logger = InlineLogger("Wildcards")

    private var changes = false

    private lateinit var npcDefinitions: NPCTypes
    private lateinit var objectDefinitions: ObjectDefinitions
    private lateinit var interfaceDefinitions: InterfaceDefinitions
    private lateinit var itemDefinitions: ItemDefinitions
    private lateinit var variableDefinitions: VariableDefinitions

    fun load(
        path: String,
        npcDefinitions: NPCTypes = NPCTypes,
        objectDefinitions: ObjectDefinitions = get(),
        interfaceDefinitions: InterfaceDefinitions = get(),
        itemDefinitions: ItemDefinitions = get(),
        variableDefinitions: VariableDefinitions = get(),
    ) {
        timedLoad("wildcard") {
            this.npcDefinitions = npcDefinitions
            this.objectDefinitions = objectDefinitions
            this.interfaceDefinitions = interfaceDefinitions
            this.itemDefinitions = itemDefinitions
            this.variableDefinitions = variableDefinitions
            val file = File(path)
            if (!file.exists()) {
                return@timedLoad 0
            }
            val reader = file.bufferedReader()
            var type = Wildcard.Npc
            fun header(line: String) {
                val (key, fingerprint) = line.split("=")
                type = Wildcard.valueOf(key.removePrefix("# ").lowercase().replaceFirstChar { it.uppercase() })
                if (fingerprint.toInt() != fingerprint(type)) {
                    logger.debug { "Invalidating $type wildcards." }
                    while (reader.ready()) {
                        val l = reader.readLine() ?: break
                        if (l.startsWith("#")) {
                            header(l)
                            break
                        }
                        continue
                    }
                }
            }
            while (reader.ready()) {
                val line = reader.readLine() ?: break
                if (line.startsWith("#")) {
                    header(line)
                    continue
                }
                insert(line, type)
            }
            npcs.size + objects.size + interfaces.size + components.size + items.size + variables.size
        }
    }

    private fun insert(line: String, type: Wildcard) {
        val (key, values) = line.split("|")
        map(type)[key] = values.split(",")
    }

    private fun fingerprint(type: Wildcard): Int {
        return when (type) {
            Wildcard.Npc -> npcDefinitions.ids.keys.hashCode()
            Wildcard.Object -> objectDefinitions.ids.keys.hashCode()
            Wildcard.Interface -> interfaceDefinitions.ids.keys.hashCode()
            Wildcard.Component -> interfaceDefinitions.componentIds.keys.hashCode()
            Wildcard.Item -> itemDefinitions.ids.keys.hashCode()
            Wildcard.Variables -> variableDefinitions.definitions.keys.hashCode()
        }
    }

    fun find(key: String, type: Wildcard, block: (String) -> Unit) {
        if (key == "*") {
            block(key)
            return
        }
        var start = 0
        var wildcard = false
        for (i in key.indices) {
            val char = key[i]
            when (char) {
                '*', '#' -> wildcard = true
                ',' -> {
                    val id = key.substring(start, i)
                    if (wildcard) {
                        for (resolved in map(type).getOrPut(id) { resolve(id, type) }) {
                            block(resolved)
                        }
                        wildcard = false
                    } else {
                        block(id)
                    }
                    start = i + 1
                }
            }
        }
        if (start < key.length) {
            val id = key.substring(start)
            if (wildcard) {
                for (resolved in map(type).getOrPut(id) { resolve(id, type) }) {
                    block(resolved)
                }
            } else {
                block(id)
            }
        }
    }

    private fun map(type: Wildcard): MutableMap<String, List<String>> = when (type) {
        Wildcard.Npc -> npcs
        Wildcard.Object -> objects
        Wildcard.Interface -> interfaces
        Wildcard.Component -> components
        Wildcard.Item -> items
        Wildcard.Variables -> variables
    }

    private fun set(type: Wildcard): Set<String> = when (type) {
        Wildcard.Npc -> npcDefinitions.ids.keys
        Wildcard.Object -> objectDefinitions.ids.keys
        Wildcard.Interface -> interfaceDefinitions.ids.keys
        Wildcard.Component -> interfaceDefinitions.componentIds.keys
        Wildcard.Item -> itemDefinitions.ids.keys
        Wildcard.Variables -> variableDefinitions.definitions.keys
    }

    private fun resolve(wildcard: String, type: Wildcard): List<String> {
        val list = set(type).filter { wildcardEquals(wildcard, it) }
        require(list.isNotEmpty()) { "No matches found for ${type.name} wildcard '$wildcard'" }
        changes = true
        return list
    }

    fun update(path: String) {
        if (!changes) {
            return
        }
        val start = System.currentTimeMillis()
        val sizes = mutableListOf<String>()
        val builder = buildString {
            for (type in Wildcard.entries) {
                val map = map(type)
                if (map.isEmpty()) {
                    continue
                }
                appendLine("# ${type.name.uppercase()}=${fingerprint(type)}")
                sizes.add("${map.size} ${type.name.lowercase().plural(map.size)}")
                for ((key, list) in map.toList().sortedBy { it.first }) {
                    appendLine("$key|${list.joinToString(",")}")
                }
            }
        }
        File(path).writeText(builder)
        changes = false
        logger.info { "Saved ${sizes.joinToString(", ")} wildcards in ${System.currentTimeMillis() - start}ms" }
    }

    fun clear() {
        for (type in Wildcard.entries) {
            map(type).clear()
        }
    }
}