package world.gregs.voidps.tools.convert

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.data.QuestDefinition
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder.Companion.toIdentifier
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import java.io.File

/**
 * Prints out all quest definitions from an RS3 cache into yaml
 */
object RS3QuestConverter {

    @JvmStatic
    fun main(args: Array<String>) {
//        val definitions = QuestDefinitions().load(Yaml(), "./data/interfaces/quests.toml")
        val rs3Cache = CacheDelegate(File("./temp/cache/cache-939-1").path)
//        OpenRS2.downloadCache(cache, 1963)
        val decoder = QuestDecoderRS3().load(rs3Cache)
        for (def in decoder.sortedBy { it.listName }) {
            val extras = def.extras ?: continue
//            println(def)
            var questId = extras["1345"] as? Int
            when (def.id) {
                53 -> questId = 159
            }
            questId ?: continue
            // Filter
            if (extras.containsKey("7834")) {
                // Filter by date
                val year = extras["7834"] as Int
                if (year > 2011) {
                    continue
                }
                if (year == 2011 && extras["7835"] != 1) {
                    continue
                }
            }
            val identifier = identifier(def)
            /*if(definitions.get(identifier).id != questId) {
                println("Mismatch: $questId ${definitions.get(identifier).id} $identifier")
                continue
            }
            if (definitions.get(questId).stringId != identifier) {
                println("Mismatch: $questId \"${definitions.get(questId).stringId}\" \"$identifier\" ${definitions.definitions.firstOrNull { it.stringId == identifier }}")
                continue
            }*/
            println(
                """
                $identifier:
                  id: $questId
                  name: "${def.name}"
                """.trimIndent(),
            )
            if (def.difficulty != -1) {
                println("  difficulty: ${def.difficulty}")
            }
            if (extras.containsKey("7855")) {
                println("  length: ${extras["7855"]}")
            }
            if (def.members) {
                println("  members: true")
            }
            if (def.subQuest != -1) {
                println("  sub_quest: ${identifier(decoder[def.subQuest])}")
            }
            if (extras.containsKey("7829")) {
                if ((extras["7829"] as Int) < 4545) {
                    println("  sprite: ${extras["7829"]}")
                }
            }
            if (def.itemSprite != -1) {
                println("  item_sprite: ${def.itemSprite}")
            }
            if (extras.containsKey("7854")) {
                println("  region: ${extras["7854"]}")
            }
            if (extras.containsKey("7814")) {
                println("  start_point: \"${extras["7814"]}\"")
            }
            if (def.pathStart != null) {
                println("  start_path:")
                for (tile in def.pathStart!!) {
                    println("    - [ ${Tile.x(tile)}, ${Tile.y(tile)}${if (Tile.level(tile) != 0) ", ${Tile.level(tile)}" else ""} ]")
                }
            }
            if (def.otherPathStart != -1) {
                val tile = def.otherPathStart
                println("  alternate_path: [ ${Tile.x(tile)}, ${Tile.y(tile)}${if (Tile.level(tile) != 0) ", ${Tile.level(tile)}" else ""} ]")
            }
            if (def.questPointRequirement != 0) {
                println("  req_quest_points: ${def.questPointRequirement}")
            }
            if (def.skillRequirements != null) {
                println("  req_skills:")
                for ((skill, level) in def.skillRequirements!!) {
                    if (skill < 25) {
                        println("    ${Skill.all.getOrNull(skill)}: $level")
                    }
                }
            }
            if (def.questRequirements != null) {
                println("  req_quests:")
                for (questIdx in def.questRequirements!!) {
                    println("    - ${identifier(decoder[questIdx])}")
                }
            }
            if (extras.containsKey("7815")) {
                println("  req_items: \"${extras["7815"]}\"")
            }
            if (extras.containsKey("7816")) {
                println("  req_combat: \"${extras["7816"]}\"")
            }
            if (def.questPoints != 0) {
                println("  points: ${def.questPoints}")
            }
            if (extras.containsKey("7158")) {
                println("  achievement: ${extras["7158"]}")
            }
            if (extras.containsKey("7823")) {
                println("  reward: \"${extras["7823"]}\"")
            }
            if (extras.containsKey("7818")) {
                println("  xp: \"${extras["7818"]}\"")
            }
            if (def.varps != null) {
                println("  varps:")
                for (varps in def.varps!!) {
                    println("    - ${varps.contentToString()}")
                }
            }
            if (def.varbits != null) {
                println("  varbits:")
                for (varbits in def.varbits!!) {
                    println("    - ${varbits.contentToString()}")
                }
            }
        }
    }

    private fun identifier(def: QuestDefinition) = toIdentifier(def.name!!.replace("The ", "").replace("A ", "").replace(":", "").trim())
}
