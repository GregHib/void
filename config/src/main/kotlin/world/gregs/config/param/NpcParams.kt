package world.gregs.config.param

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.config.param.codec.BooleanParam
import world.gregs.config.param.codec.DoubleParam
import world.gregs.config.param.codec.IntParam
import world.gregs.config.param.codec.ParamCodec
import world.gregs.config.param.codec.StringListParam
import world.gregs.config.param.codec.StringParam
import kotlin.collections.iterator

object NpcParams : Parameters() {

    override val codecs = mapOf(
        Params.CLONE to StringParam,
        Params.ID to IntParam,
        Params.EXAMINE to StringParam,
        Params.CATEGORIES to StringListParam,
        Params.WANDER_RANGE to IntParam,
        Params.INTERACT_RANGE to IntParam,
        Params.HITPOINTS to IntParam,
        Params.ATT to IntParam,
        Params.STR to IntParam,
        Params.DEF to IntParam,
        Params.COMBAT_DEF to StringParam,
        Params.STYLE to StringParam, // Deprecated
        Params.DEFEND_ANIM to StringParam, // Deprecated
        Params.DEATH_ANIM to StringParam, // Deprecated
        Params.MAX_HIT_MELEE to IntParam, // Deprecated
        Params.MAX_HIT_MAGIC to IntParam,
        Params.MAX_HIT_RANGE to IntParam,
        Params.MAX_HIT_CRUSH to IntParam,
        Params.MAX_HIT_STAB to IntParam,
        Params.MAX_HIT_DRAGONFIRE to IntParam,
        Params.MAX_HIT_SLASH to IntParam,
        Params.RESPAWN_DELAY to IntParam,
        Params.DROP_TABLE to StringParam,
        Params.MAGE to IntParam,
        Params.HUNT_MODE to StringParam,
        Params.ATTACK_BONUS to IntParam,
        Params.XP_BONUS to DoubleParam,
        Params.ATTACK_SPEED to IntParam,
        Params.HUNT_RANGE to IntParam,
        Params.COLLISION to StringParam,
        Params.SHOP to StringParam,
        Params.RANGE to IntParam,
        Params.SLAYER_XP to DoubleParam,
        Params.BAR_CRAWL to BarCrawl(),
        Params.PATCH to StringParam,
        Params.NORTH_PATCH to StringParam,
        Params.SOUTH_PATCH to StringParam,
        Params.SPELL to StringParam,
        Params.PICKPOCKET to PickpocketParam(),
        Params.REGEN_RATE_TICKS to IntParam,
        Params.SLAYER_LEVEL to IntParam,
        Params.WEAPON_STYLE to StringParam,
        Params.AMMO to StringParam,
        Params.ATTACK_RANGE to IntParam,
        Params.RETALIATES to BooleanParam,
        Params.HEIGHT to IntParam,
        Params.RANGED_STRENGTH to IntParam,
        Params.SOLID to BooleanParam,
        Params.LARGE_HEAD to BooleanParam,
        Params.DIALOGUE to StringParam,
        Params.AKA to StringListParam,
        Params.IMMUNE_POISON to BooleanParam,
        Params.IMMUNE_CANNON to BooleanParam,
        Params.IMMUNE_DEATH to BooleanParam,
        Params.IMMUNE_DEFLECT to BooleanParam,
        Params.IMMUNE_STUN to BooleanParam,
        Params.IMMUNE_DRAIN to BooleanParam,
        Params.IMMUNE_DISEASE to BooleanParam,
        Params.INTERACTS to BooleanParam,
        Params.GOD to StringParam,
        Params.DROPS to StringParam,
        Params.MAGIC to IntParam,
        Params.ALLOWED_UNDER to BooleanParam,
        Params.CRAWL to BooleanParam,
        Params.SONG to IntParam,
        Params.POISON to IntParam,
        Params.RETREAT_RANGE to IntParam, // deprecated
        Params.STUCK_LIMIT to IntParam,
        Params.DAMAGE_CAP to IntParam,
        Params.FISHING to FishingParam(),
    )

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