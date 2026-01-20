package world.gregs.voidps.engine.data.param

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.data.NpcType
import world.gregs.voidps.engine.data.definition.data.Pocket
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.data.param.codec.BooleanParam
import world.gregs.voidps.engine.data.param.codec.DoubleParam
import world.gregs.voidps.engine.data.param.codec.IntParam
import world.gregs.voidps.engine.data.param.codec.ParamCodec
import world.gregs.voidps.engine.data.param.codec.StringListParam
import world.gregs.voidps.engine.data.param.codec.StringParam
import kotlin.collections.iterator

object NpcParams : Parameters<NpcType>() {

    override val codecs = mapOf(
        CLONE to StringParam,
        ID to IntParam,
        EXAMINE to StringParam,
        CATEGORIES to StringListParam,
        WANDER_RANGE to IntParam,
        INTERACT_RANGE to IntParam,
        HITPOINTS to IntParam,
        ATT to IntParam,
        STR to IntParam,
        DEF to IntParam,
        COMBAT_DEF to StringParam,
        STYLE to StringParam, // Deprecated
        DEFEND_ANIM to StringParam, // Deprecated
        DEATH_ANIM to StringParam, // Deprecated
        MAX_HIT_MELEE to IntParam, // Deprecated
        MAX_HIT_MAGIC to IntParam,
        MAX_HIT_RANGE to IntParam,
        MAX_HIT_CRUSH to IntParam,
        MAX_HIT_STAB to IntParam,
        MAX_HIT_DRAGONFIRE to IntParam,
        MAX_HIT_SLASH to IntParam,
        RESPAWN_DELAY to IntParam,
        DROP_TABLE to StringParam,
        MAGE to IntParam,
        HUNT_MODE to StringParam,
        ATTACK_BONUS to IntParam,
        XP_BONUS to DoubleParam,
        ATTACK_SPEED to IntParam,
        HUNT_RANGE to IntParam,
        COLLISION to StringParam,
        SHOP to StringParam,
        RANGE to IntParam,
        SLAYER_XP to DoubleParam,
        BAR_CRAWL to BarCrawl(),
        PATCH to StringParam,
        NORTH_PATCH to StringParam,
        SOUTH_PATCH to StringParam,
        SPELL to StringParam,
        PICKPOCKET to PickpocketParam(),
        REGEN_RATE_TICKS to IntParam,
        SLAYER_LEVEL to IntParam,
        WEAPON_STYLE to StringParam,
        AMMO to StringParam,
        ATTACK_RANGE to IntParam,
        RETALIATES to BooleanParam,
        HEIGHT to IntParam,
        RANGED_STRENGTH to IntParam,
        SOLID to BooleanParam,
        LARGE_HEAD to BooleanParam,
        DIALOGUE to StringParam,
        AKA to StringListParam,
        IMMUNE_POISON to BooleanParam,
        IMMUNE_CANNON to BooleanParam,
        IMMUNE_DEATH to BooleanParam,
        IMMUNE_DEFLECT to BooleanParam,
        IMMUNE_STUN to BooleanParam,
        IMMUNE_DRAIN to BooleanParam,
        INTERACTS to BooleanParam,
        GOD to StringParam,
        DROPS to StringParam,
        MAGIC to IntParam,
        ALLOWED_UNDER to BooleanParam,
        SONG to IntParam,
        POISON to IntParam,
        FISHING to FishingParam(),
    )

    override val keys = mapOf(
        "clone" to CLONE,
        "id" to ID,
        "examine" to EXAMINE,
        "categories" to CATEGORIES,
        "wander_range" to WANDER_RANGE,
        "interact_range" to INTERACT_RANGE,
        "hitpoints" to HITPOINTS,
        "att" to ATT,
        "str" to STR,
        "def" to DEF,
        "combat_def" to COMBAT_DEF,
        "style" to STYLE,
        "defend_anim" to DEFEND_ANIM,
        "death_anim" to DEATH_ANIM,
        "max_hit_melee" to MAX_HIT_MELEE,
        "max_hit_magic" to MAX_HIT_MAGIC,
        "max_hit_range" to MAX_HIT_RANGE,
        "max_hit_crush" to MAX_HIT_CRUSH,
        "max_hit_stab" to MAX_HIT_STAB,
        "max_hit_dragonfire" to MAX_HIT_DRAGONFIRE,
        "max_hit_slash" to MAX_HIT_SLASH,
        "respawn_delay" to RESPAWN_DELAY,
        "drop_table" to DROP_TABLE,
        "mage" to MAGE,
        "hunt_mode" to HUNT_MODE,
        "attack_bonus" to ATTACK_BONUS,
        "xp_bonus" to XP_BONUS,
        "attack_speed" to ATTACK_SPEED,
        "hunt_range" to HUNT_RANGE,
        "collision" to COLLISION,
        "shop" to SHOP,
        "range" to RANGE,
        "slayer_xp" to SLAYER_XP,
        "bar_crawl" to BAR_CRAWL,
        "patch" to PATCH,
        "north_patch" to NORTH_PATCH,
        "south_patch" to SOUTH_PATCH,
        "spell" to SPELL,
        "pickpocket" to PICKPOCKET,
        "regen_rate_ticks" to REGEN_RATE_TICKS,
        "slayer_level" to SLAYER_LEVEL,
        "weapon_style" to WEAPON_STYLE,
        "ammo" to AMMO,
        "attack_range" to ATTACK_RANGE,
        "retaliates" to RETALIATES,
        "height" to HEIGHT,
        "ranged_strength" to RANGED_STRENGTH,
        "solid" to SOLID,
        "large_head" to LARGE_HEAD,
        "dialogue" to DIALOGUE,
        "aka" to AKA,
        "immune_poison" to IMMUNE_POISON,
        "immune_cannon" to IMMUNE_CANNON,
        "immune_death" to IMMUNE_DEATH,
        "immune_deflect" to IMMUNE_DEFLECT,
        "immune_stun" to IMMUNE_STUN,
        "immune_drain" to IMMUNE_DRAIN,
        "interacts" to INTERACTS,
        "god" to GOD,
        "drops" to DROPS,
        "magic" to MAGIC,
        "allowed_under" to ALLOWED_UNDER,
        "song" to SONG,
        "poison" to POISON,
        "fishing" to FISHING,
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

        override fun read(reader: ConfigReader) = Pocket.Companion(reader)
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
                val spot = Spot.Companion(reader)
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