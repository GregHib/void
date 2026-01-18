package world.gregs.voidps.tools.convert

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.readValue
import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import java.io.File

@Suppress("UNCHECKED_CAST")
object SkillDataConverter {
    @Suppress("USELESS_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()

        val koin = startKoin {
            modules(
                module {
                    single { ItemDefinitions.init(ItemDecoder().load(get())).load(listOf(Settings["definitions.items"])) }
                    single { CacheDelegate(Settings["storage.cache.path"]) as Cache }
                },
            )
        }.koin

        val cache: Cache = koin.get()
        val files = configFiles()
        val sounds = SoundDefinitions().load(files.list(Settings["definitions.sounds"]))
        val animations = AnimationDefinitions(AnimationDecoder().load(cache)).load(listOf(Settings["definitions.animations"]))
//        var decoder = InventoryDecoder(koin.get())
        val mapper = ObjectMapper()
        val yaml = ObjectMapper(
            YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).apply {
                enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                disable(YAMLGenerator.Feature.SPLIT_LINES)
                enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
            },
        )
        val data: Map<String, Map<String, Any>> = mapper.readValue(File("./data/dump/SkillData.json"))
        val newAnimations = mutableMapOf<Int, MutableList<String>>()
        val newSounds = mutableMapOf<Int, MutableList<String>>()
        for ((name, map) in data) {
            val map = map.toMutableMap()
            if (map.containsKey("skill")) {
                map.replace("skill", Skill.entries[map["skill"] as Int])
                if (map["skill"] != Skill.Crafting) {
                    continue
                }
            } else {
                continue
            }
            if (map.containsKey("game message")) {
                map["message"] = map.getOrDefault("game message", "")
                map.remove("game message")
            }
            if (map.containsKey("animation")) {
                val string = animations.get(map["animation"] as Int).stringId
                map.replace("animation", string)
                if (string.isEmpty()) {
                    newAnimations.getOrPut(map["animation"] as Int) { mutableListOf() }.add(name)
                } else if (string.toIntOrNull() != null) {
                    newAnimations.getOrPut(string.toInt()) { mutableListOf() }.add(name)
                }
            }
            if (map.containsKey("sound")) {
                val string = sounds.get(map["sound"] as Int).stringId
                map.replace("sound", string)
                if (string.isEmpty()) {
                    newSounds.getOrPut(map["sound"] as Int) { mutableListOf() }.add(name)
                } else if (string.toIntOrNull() != null) {
                    newSounds.getOrPut(string.toInt()) { mutableListOf() }.add(name)
                }
            }
            if (map.containsKey("tool")) {
                map.replace("tool", ItemDefinitions.get(map["tool"] as Int).stringId)
            }
            if (map.containsKey("product")) {
                map.replace("product", ItemDefinitions.get(map["product"] as Int).stringId)
            }
            if (map.containsKey("fail product")) {
                map.replace("fail product", ItemDefinitions.get(map["fail product"] as Int).stringId)
            }
            if (map.containsKey("materials")) {
                val list: List<Int> = map["materials"] as List<Int>
                map.replace("materials", list.map { ItemDefinitions.get(it).stringId })
            }
            if (map.containsKey("by-products")) {
                val list: List<Int> = map["by-products"] as List<Int>
                map.replace("by-products", list.map { ItemDefinitions.get(it).stringId })
            }
            printMaking(map, yaml)
        }
        if (newAnimations.isNotEmpty()) {
            println("Animations")
            for (a in newAnimations) {
                println(a)
            }
        }
        if (newSounds.isNotEmpty()) {
            println("Sounds")
            for (s in newSounds) {
                println(s)
            }
        }
    }

    private fun printMaking(map: MutableMap<String, Any>, yaml: ObjectMapper) {
        val str = yaml.writeValueAsString(
            ItemOnItemDefinition(
                requires = if (map.containsKey("tool")) listOf(Item(map["tool"] as String)) else emptyList(),
                skill = map["skill"] as? Skill,
                level = map["level"] as? Int ?: 1,
                remove = (map["materials"] as List<String>).map { Item(it) },
                add = listOf(Item(map["product"] as String)).union((map["by-products"] as? List<String> ?: emptyList()).map { Item(it) }).toList(),
                xp = map["experience"] as? Double ?: (map["experience"] as? Int)?.toDouble() ?: 0.0,
                ticks = map["ticks"] as Int,
                animation = map["animation"] as? String ?: "",
                sound = map["sound"] as? String ?: "",
                message = map["message"] as String ?: ItemOnItemDefinition.EMPTY.message,
            ),
        )
        println(
            "${map["product"]}:\n    ${str.replace("\n", "\n    ")
                .replace("- id: ", "- ")
                .replace("\n    skill: null", "")
                .replace("\n    level: 1\n", "\n")
                .replace("\n    xp: 0.0", "")
                .replace("\n    requires: []", "")
                .replace("\n    add: []", "")
                .replace("\n    remove: []", "")
                .replace("\n    delay: 0", "")
                .replace("\n    ticks: 0", "")
                .replace("\n    type: make", "")
                .replace("\n    animation: \"\"", "")
                .replace("\n    message: \"\"", "")
                .replace("\n    graphic: \"\"", "")
                .replace("\n    sound: \"\"", "")
                .replace("skill: Crafting", "skill: crafting")
                .replace(":\n      - ", ": [ ")
                .replace("\n      - ", ", ")
            }",
        )
    }
}
