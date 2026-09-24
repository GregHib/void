package content.skill.dungeoneering.boss

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.entity.effect.transform
import content.skill.dungeoneering.dungeonRoomBounds
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class AsteaFrostweb : Script {
    init {
        npcSpawn("rand_ancient_mage_*") {
            softTimers.start("astea_overhead")
        }

        npcTimerStart("astea_overhead") { 35 }

        npcTimerTick("astea_overhead") {
            val type = transformId.substringAfterLast("_")
            transform(
                id.replace(
                    "melee",
                    when (type) {
                        "magic" -> if (random.nextBoolean()) "melee" else "ranged"
                        "ranged" -> if (random.nextBoolean()) "melee" else "magic"
                        else -> if (random.nextBoolean()) "magic" else "ranged"
                    },
                ),
            )
            Timer.CONTINUE
        }

        npcAttack("astea_frostweb", "freeze") {
            val dir = Direction.cardinal.random(random)
            walkTo(tile.add(dir))
        }

        npcAttack("astea_frostweb", "spider") { target ->
            if (target !is Player) {
                return@npcAttack
            }
            val room = target.dungeonRoomBounds()
            val fighting = target.dungeonMembers.filter { it.tile in room }
            val amount = if (fighting.size == 1) 6 else 2
            val count = get("spider_count", 0)
            if (count >= amount * fighting.size) {
                return@npcAttack
            }
            val spider = when (id.substringBeforeLast("_")) {
                "rand_ancient_mage_0" -> "rand_ice_spider_1"
                "rand_ancient_mage_05" -> "rand_ice_spider_1" // Guess
                "rand_ancient_mage_1" -> "rand_ice_spider_1" // Guess
                "rand_ancient_mage_2" -> "rand_ice_spider_1" // Guess
                "rand_ancient_mage_3" -> "rand_ice_spider_1" // Guess
                "rand_ancient_mage_4" -> "rand_ice_spider_1" // https://www.youtube.com/watch?v=PvDKgtCVDP8
                "rand_ancient_mage_5" -> "rand_ice_spider_2"
                "rand_ancient_mage_6" -> "rand_ice_spider_6"
                "rand_ancient_mage_7" -> "rand_ice_spider_7"
                "rand_ancient_mage_8" -> "rand_ice_spider_9"
                "rand_ancient_mage_9" -> "rand_ice_spider_9"
                "rand_ancient_mage_10" -> "rand_ice_spider_10" // Guess
                else -> "rand_ice_spider_10"
            }
            for (member in fighting) {
                for (i in 0 until amount) {
                    if (member.contains("ice_spider_$i")) {
                        continue
                    }
                    val spider = NPCs.addRandom(spider, tile.toCuboid(2), ticks = TimeUnit.SECONDS.toTicks(30), owner = member) ?: continue
                    spider.interactPlayer(member, "Attack")
                    inc("spider_count")
                    set("ice_spider_$i", true)
                    break
                }
            }
        }

        npcSpawn("rand_ice_spider_*") {
            anim("ice_spider_spawn")
        }

        npcDespawn("rand_ice_spider_*") {
            anim("ice_spider_despawn")
        }

        npcDeath("rand_ice_spider_*") {
            val owner = owner ?: return@npcDeath
            it.dropItems = false
            for (i in 5 downTo 0) {
                if (owner.contains("ice_spider_$i")) {
                    owner.clear("ice_spider_$i")
                    break
                }
            }
        }

        npcDeath("rand_ancient_mage_*") {
            val bounds = dungeonRoomBounds()
            for (tile in bounds) {
                val npcs = NPCs.at(tile)
                for (npc in npcs) {
                    if (npc.id.startsWith("rand_ice_spider")) {
                        NPCs.remove(npc)
                    }
                }
            }
        }
    }
}
