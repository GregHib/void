package world.gregs.voidps.tools.wiki.scrape

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.voidps.engine.data.find
import world.gregs.voidps.tools.wiki.model.Wiki
import java.io.File
import java.io.PrintStream

object MonsterInfoBoxDumper {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val files = configFiles()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val categories = CategoryDefinitions().load(files.find(Settings["definitions.categories"]))
        val ammo = AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"]))
        val parameters = ParameterDefinitions(categories, ammo).load(files.find(Settings["definitions.parameters"]))
        val definitions = NPCDecoder(true, parameters).load(cache)
        val decoder = NPCDefinitions(definitions).load(files.getValue(Settings["definitions.npcs"]))
        val wiki = Wiki.load("C:\\Users\\Greg\\Downloads\\oldschool-runescape-wiki-2023-10-08.xml")
        val file = File("./osrs_npcs.toml")
        System.setOut(PrintStream(file.outputStream(), true))
        for (page in wiki.pages) {
            if (!page.contains("Infobox Monster")) {
                continue
            }
            val infobox = page.getTemplateMap("Infobox Monster") ?: continue
            if (infobox.contains("name1") || infobox.contains("version1")) {
                for (i in 1 until 25) {
                    if (infobox.contains("name$i") || infobox.contains("version$i")) {
                        print(decoder, infobox, i.toString())
                    } else {
                        break
                    }
                }
            } else if (infobox.contains("name")) {
                print(decoder, infobox, "")
            }
        }
    }

    val nameRegex = "[(\\[\\])']".toRegex()
    private fun print(decoder: NPCDefinitions, infobox: Map<String, Any>, suffix: String) {
        val name = infobox["name$suffix"] as? String ?: infobox["name"] as? String ?: return
        val release = infobox["release$suffix"] as? String ?: infobox["release"] as? String

        if (release != null) {
            val date = release.replace(nameRegex, "").toSnakeCase()
            for (year in 2013..2025) {
                if (date.endsWith("_$year")) {
                    return
                }
            }
        }
        val version = infobox["version$suffix"] as? String ?: infobox["version"] as? String
        if (version != null) {
            println("[${name.replace(nameRegex, "").toSnakeCase()}_${version.replace(nameRegex, "").toSnakeCase()}]")
        } else {
            println("[${name.replace(nameRegex, "").toSnakeCase()}]")
        }
        val id = infobox["id$suffix"] as? String ?: infobox["id"] as? String
        if (id != null) {
            val ids = if (version != null && version.startsWith("Level ", true)) {
                val level = version.substringAfter("evel ").substringBefore(" ").toInt()
                decoder.definitions.filter { it.name.contains(name, true) && it.combat == level && it.options.contains("Attack") }
            } else {
                decoder.definitions.filter { it.name.contains(name, true) && it.options.contains("Attack") }
            }
            if (ids.isNotEmpty()) {
                println("id = ${ids.joinToString(",") { it.stringId }}")
            }
        }
        val hp = infobox["hitpoints$suffix"] as? String ?: infobox["hitpoints"] as? String
        if (hp != null && hp.isNotBlank()) {
            println("hitpoints = ${hp.toIntOrNull()?.let { it * 10 } ?: hp}")
        }
        val att = infobox["att$suffix"] as? String ?: infobox["att"] as? String
        if (att != null && att.isNotBlank() && att != "1") {
            println("att = $att")
        }
        val str = infobox["str$suffix"] as? String ?: infobox["str"] as? String
        if (str != null && str.isNotBlank() && str != "1") {
            println("str = $str")
        }
        val def = infobox["def$suffix"] as? String ?: infobox["def"] as? String
        if (def != null && def.isNotBlank() && def != "1") {
            println("def = $def")
        }
        val mage = infobox["mage$suffix"] as? String ?: infobox["mage"] as? String
        if (mage != null && mage.isNotBlank() && mage != "1") {
            println("mage = $mage")
        }
        val range = infobox["range$suffix"] as? String ?: infobox["range"] as? String
        if (range != null && range.isNotBlank() && range != "1") {
            println("range = $range")
        }
        val attSpeed = infobox["attack speed$suffix"] as? String ?: infobox["attack speed"] as? String
        if (attSpeed != null && attSpeed.isNotBlank() && attSpeed != "4") {
            println("attack_speed = $attSpeed")
        }
        val maxHit = infobox["max hit$suffix"] as? String ?: infobox["max hit"] as? String
        val style = infobox["attack style$suffix"] as? String ?: infobox["attack style"] as? String
        if (style != null && style.isNotBlank() && style != "0") {
            if (style.contains(",")) {
                for (part in style.split(",")) {
                    println("style = \"${part.trim().replace(nameRegex, "").toSnakeCase()}\"")
                }
                if (maxHit != null && maxHit.contains(",")) {
                    for (part in maxHit.split(",")) {
                        val type = part.trim().substringAfter(" ").replace(nameRegex, "").toSnakeCase()
                        println("max_hit_$type = ${part.trim().substringBefore(" ").toIntOrNull()?.let { it * 10 } ?: part}")
                    }
                } else if (maxHit != null) {
                    println("max_hit_melee = ${maxHit.toIntOrNull()?.let { it * 10 } ?: maxHit}")
                }
            } else {
                println("style = \"${style.replace(nameRegex, "").toSnakeCase()}\"")
                if (maxHit != null) {
                    println("max_hit_melee = ${maxHit.toIntOrNull()?.let { it * 10 } ?: maxHit}")
                }
            }
        }
        val attbns = infobox["attbns$suffix"] as? String ?: infobox["attbns"] as? String
        if (attbns != null && attbns.isNotBlank() && attbns != "0") {
            println("attack_bonus = $attbns")
        }
        val xpBonus = infobox["xpbonus$suffix"] as? String ?: infobox["xpbonus"] as? String
        if (xpBonus != null && xpBonus.isNotBlank() && xpBonus != "0") {
            println("xp_bonus = ${xpBonus.toIntOrNull()?.let { it / 10.0 } ?: xpBonus}")
        }
        val mbns = infobox["mbns$suffix"] as? String ?: infobox["mbns"] as? String
        if (mbns != null && mbns.isNotBlank() && mbns != "0") {
            println("# Unexpected magic bonus $mbns")
        }
        val rngbns = infobox["rngbns$suffix"] as? String ?: infobox["rngbns"] as? String
        if (rngbns != null && rngbns.isNotBlank() && rngbns != "0") {
            println("ranged_strength = $rngbns")
        }
        val aggressive = infobox["aggressive$suffix"] as? String ?: infobox["aggressive"] as? String
        if (aggressive != null && aggressive.equals("yes", true)) {
            println("hunt_mode = \"cowardly\"")
        }
        val slayer = infobox["slayxp$suffix"] as? String ?: infobox["slayxp"] as? String
        if (slayer != null) {
            val xp = slayer.toDoubleOrNull()
            if (xp == null) {
                println("// slayer_xp = $slayer")
            } else {
                println("slayer_xp = $xp")
            }
        }
        val category = infobox["cat$suffix"] as? String ?: infobox["cat"] as? String
        if (category != null) {
            if (category.contains(",")) {
                for (part in category.split(",")) {
                    println("categories = [\"${part.trim().toSnakeCase()}\"]")
                }
            } else {
                println("categories = [\"${category.toSnakeCase()}\"]")
            }
        }
        val poison = infobox["immunepoison$suffix"] as? String ?: infobox["immunepoison"] as? String
        if (poison != null && poison.equals("yes", true)) {
            println("immune_poison = true")
        }
        val cannon = infobox["immunecannon$suffix"] as? String ?: infobox["immunecannon"] as? String
        if (cannon != null && cannon.equals("yes", true)) {
            println("immune_cannon = true")
        }
        val members = (infobox["members$suffix"] as? String)?.toBoolean() ?: (infobox["members"] as? String)?.toBoolean() ?: false
        if (members) {
            println("members = true")
        }
        val aka = infobox["aka$suffix"] as? String ?: infobox["aka"] as? String
        if (aka != null) {
            println("aka = [\"${aka.toSnakeCase()}\"]")
        }
        val respawn = infobox["respawn$suffix"] as? String ?: infobox["respawn"] as? String
        if (respawn != null) {
            println("respawn_delay = $respawn")
        }
        val examine = infobox["examine$suffix"] as? String ?: infobox["examine"] as? String
        if (examine != null) {
            println("examine = \"$examine\"")
        }
        println()
    }
}
