package world.gregs.voidps.engine.data.types.keys

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.NpcType
import world.gregs.voidps.engine.data.definition.data.Pocket
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.data.param.Parameters
import world.gregs.voidps.engine.data.param.codec.BooleanParam
import world.gregs.voidps.engine.data.param.codec.DoubleParam
import world.gregs.voidps.engine.data.param.codec.IntParam
import world.gregs.voidps.engine.data.param.codec.ParamCodec
import world.gregs.voidps.engine.data.param.codec.StringListParam
import world.gregs.voidps.engine.data.param.codec.StringParam
import kotlin.collections.set

object NpcParams : Parameters<NpcType>() {

    override val parameters = listOf(
        Triple("clone", CLONE, StringParam),
        Triple("id", ID, IntParam),
        Triple("examine", EXAMINE, StringParam),
        Triple("categories", CATEGORIES, StringListParam),
        Triple("wander_range", WANDER_RANGE, IntParam),
        Triple("interact_range", INTERACT_RANGE, IntParam),
        Triple("hitpoints", HITPOINTS, IntParam),
        Triple("att", ATT, IntParam),
        Triple("str", STR, IntParam),
        Triple("def", DEF, IntParam),
        Triple("combat_def", COMBAT_DEF, StringParam),
        Triple("style", STYLE, StringParam), // Deprecated
        Triple("defend_anim", DEFEND_ANIM, StringParam), // Deprecated
        Triple("death_anim", DEATH_ANIM, StringParam), // Deprecated
        Triple("max_hit_melee", MAX_HIT_MELEE, IntParam), // Deprecated
        Triple("max_hit_magic", MAX_HIT_MAGIC, IntParam),
        Triple("max_hit_range", MAX_HIT_RANGE, IntParam),
        Triple("max_hit_crush", MAX_HIT_CRUSH, IntParam),
        Triple("max_hit_stab", MAX_HIT_STAB, IntParam),
        Triple("max_hit_dragonfire", MAX_HIT_DRAGONFIRE, IntParam),
        Triple("max_hit_slash", MAX_HIT_SLASH, IntParam),
        Triple("respawn_delay", RESPAWN_DELAY, IntParam),
        Triple("drop_table", DROP_TABLE, StringParam),
        Triple("mage", MAGE, IntParam),
        Triple("hunt_mode", HUNT_MODE, StringParam),
        Triple("attack_bonus", ATTACK_BONUS, IntParam),
        Triple("xp_bonus", XP_BONUS, DoubleParam),
        Triple("attack_speed", ATTACK_SPEED, IntParam),
        Triple("hunt_range", HUNT_RANGE, IntParam),
        Triple("collision", COLLISION, StringParam),
        Triple("shop", SHOP, StringParam),
        Triple("range", RANGE, IntParam),
        Triple("slayer_xp", SLAYER_XP, DoubleParam),
        Triple("bar_crawl", BAR_CRAWL, BarCrawl()),
        Triple("patch", PATCH, StringParam),
        Triple("north_patch", NORTH_PATCH, StringParam),
        Triple("south_patch", SOUTH_PATCH, StringParam),
        Triple("spell", SPELL, StringParam),
        Triple("pickpocket", PICKPOCKET, PickpocketParam()),
        Triple("regen_rate_ticks", REGEN_RATE_TICKS, IntParam),
        Triple("slayer_level", SLAYER_LEVEL, IntParam),
        Triple("weapon_style", WEAPON_STYLE, StringParam),
        Triple("ammo", AMMO, StringParam),
        Triple("attack_range", ATTACK_RANGE, IntParam),
        Triple("retaliates", RETALIATES, BooleanParam),
        Triple("height", HEIGHT, IntParam),
        Triple("ranged_strength", RANGED_STRENGTH, IntParam),
        Triple("solid", SOLID, BooleanParam),
        Triple("large_head", LARGE_HEAD, BooleanParam),
        Triple("dialogue", DIALOGUE, StringParam),
        Triple("aka", AKA, StringListParam),
        Triple("immune_poison", IMMUNE_POISON, BooleanParam),
        Triple("immune_cannon", IMMUNE_CANNON, BooleanParam),
        Triple("immune_death", IMMUNE_DEATH, BooleanParam),
        Triple("immune_deflect", IMMUNE_DEFLECT, BooleanParam),
        Triple("immune_stun", IMMUNE_STUN, BooleanParam),
        Triple("immune_drain", IMMUNE_DRAIN, BooleanParam),
        Triple("interacts", INTERACTS, BooleanParam),
        Triple("god", GOD, StringParam),
        Triple("drops", DROPS, StringParam),
        Triple("magic", MAGIC, IntParam),
        Triple("allowed_under", ALLOWED_UNDER, BooleanParam),
        Triple("song", SONG, IntParam),
        Triple("poison", POISON, IntParam),
        Triple("fishing", FISHING, FishingParam()),
    )

    const val EXAMINE = 10_000
    const val CATEGORIES = 10_001
    const val WANDER_RANGE = 10_002
    const val INTERACT_RANGE = 10_003
    const val HITPOINTS = 10_004
    const val ATT = 10_005
    const val STR = 10_006
    const val DEF = 10_007
    const val STYLE = 10_008
    const val MAX_HIT_MELEE = 10_009
    const val RESPAWN_DELAY = 10_010
    const val DROP_TABLE = 10_011
    const val MAGE = 10_012
    const val HUNT_MODE = 10_013
    const val ATTACK_BONUS = 10_014
    const val XP_BONUS = 10_015
    const val IMMUNE_POISON = 10_016
    const val ATTACK_SPEED = 10_017
    const val HUNT_RANGE = 10_018
    const val COLLISION = 10_019
    const val SHOP = 10_020
    const val RANGE = 10_021
    const val PATCH = 10_022
    const val SLAYER_XP = 10_023
    const val BAR_CRAWL = 10_024
    const val NORTH_PATCH = 10_025
    const val SOUTH_PATCH = 10_026
    const val SPELL = 10_027
    const val MAX_HIT_MAGIC = 10_028
    const val COMBAT_DEF = 10_029
    const val PICKPOCKET = 10_030
    const val MAX_HIT_CRUSH = 10_031
    const val REGEN_RATE_TICKS = 10_032
    const val SLAYER_LEVEL = 10_033
    const val WEAPON_STYLE = 10_034
    const val AMMO = 10_035
    const val ATTACK_RANGE = 10_036
    const val MAX_HIT_RANGE = 10_037
    const val RETALIATES = 10_041
    const val DEATH_ANIM = 10_042
    const val MAX_HIT_DRAGONFIRE = 10_043
    const val HEIGHT = 10_044
    const val RANGED_STRENGTH = 10_045
    const val MAX_HIT_STAB = 10_046
    const val IMMUNE_CANNON = 10_049
    const val MAX_HIT_SLASH = 10_050
    const val SOLID = 10_052
    const val LARGE_HEAD = 10_053
    const val DEFEND_ANIM = 10_054
    const val DIALOGUE = 10_055
    const val AKA = 10_056
    const val IMMUNE_DEATH = 10_058
    const val IMMUNE_DEFLECT = 10_059
    const val IMMUNE_STUN = 10_060
    const val IMMUNE_DRAIN = 10_061
    const val INTERACTS = 10_064
    const val GOD = 10_069
    const val DROPS = 10_070
    const val MAGIC = 10_071
    const val ALLOWED_UNDER = 10_072
    const val SONG = 10_074
    const val POISON = 10_075
    const val FISHING = 10_077


    class PickpocketParam : ParamCodec<Pocket>() {
        override fun read(reader: Reader) = Pocket(
            level = reader.readByte(),
            xp = DoubleParam.read(reader),
            stunHit = reader.readShort()..reader.readShort(),
            stunTicks = reader.readByte(),
            chance = reader.readUnsignedByte()..reader.readUnsignedByte(),
            caughtMessage = reader.readString(),
            table = reader.readString()
        )

        override fun read(reader: ConfigReader) = Pocket(reader)
        override fun write(writer: Writer, value: Pocket) {
            writer.writeByte(value.level)
            DoubleParam.write(writer, value.xp)
            writer.writeShort(value.stunHit.first)
            writer.writeShort(value.stunHit.last)
            writer.writeByte(value.stunTicks)
            writer.writeByte(value.chance.first)
            writer.writeByte(value.chance.last)
            writer.writeString(value.caughtMessage)
            writer.writeString(value.table)
        }
    }

    class BarCrawl : ParamCodec<Map<String, Any>>() {
        override fun read(reader: Reader): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            map["id"] = reader.readString()
            val start = reader.readString()
            if (start != "") {
                map["start"] = start
            }
            map["price"] = reader.readInt()
            map["insufficient"] = reader.readString()
            map["give"] = reader.readString()
            map["drink"] = reader.readString()
            map["effect"] = reader.readString()
            val sign = reader.readString()
            if (sign != "") {
                map["sign"] = sign
            }
            return map
        }

        override fun read(reader: ConfigReader) = reader.map()
        override fun write(writer: Writer, value: Map<String, Any>) {
            writer.writeString(value.getValue("id") as String)
            writer.writeString(value["start"] as? String)
            writer.writeInt(value.getValue("price") as Int)
            writer.writeString(value.getValue("insufficient") as String)
            writer.writeString(value.getValue("give") as String)
            writer.writeString(value.getValue("drink") as String)
            writer.writeString(value.getValue("effect") as String)
            writer.writeString(value["sign"] as? String)
        }
    }

    class FishingParam : ParamCodec<Map<String, Spot>>() {
        override fun read(reader: Reader): Map<String, Spot> {
            val length = reader.readByte()
            val spots = Object2ObjectOpenHashMap<String, Spot>(length)
            for (a in 0 until length) {
                val key = reader.readString()
                var size = reader.readByte()
                val tackle = ObjectArrayList<String>()
                for (i in 0 until size) {
                    tackle += reader.readString()
                }
                size = reader.readByte()
                val bait = Object2ObjectOpenHashMap<String, MutableList<String>>()
                for (i in 0 until size) {
                    val name = reader.readString()
                    val listSize = reader.readByte()
                    val list = mutableListOf<String>()
                    for (j in 0 until listSize) {
                        list += reader.readString()
                    }
                    bait[name] = list
                }
                spots[key] = Spot(tackle, bait)
            }
            return spots
        }

        override fun read(reader: ConfigReader): Map<String, Spot> {
            val spots = Object2ObjectOpenHashMap<String, Spot>(2, Hash.VERY_FAST_LOAD_FACTOR)
            while (reader.nextEntry()) {
                val type = reader.key()
                val spot = Spot(reader)
                spots[type] = spot
            }
            return spots
        }

        override fun write(writer: Writer, value: Map<String, Spot>) {
            writer.writeByte(value.size)
            for ((key, spot) in value) {
                writer.writeString(key)
                writer.writeByte(spot.tackle.size)
                for (tackle in spot.tackle) {
                    writer.writeString(tackle)
                }
                writer.writeByte(spot.bait.size)
                for ((name, list) in spot.bait) {
                    writer.writeString(name)
                    writer.writeByte(list.size)
                    for (item in list) {
                        writer.writeString(item)
                    }
                }
            }
        }
    }
}


