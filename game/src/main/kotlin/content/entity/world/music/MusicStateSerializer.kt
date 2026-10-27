package content.entity.world.music

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.engine.data.snapshot.serializer.AreaSerializer

internal object MusicStateSerializer : KSerializer<MusicState> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("content.entity.world.music.MusicState") {
        element("id", Int.serializer().descriptor)
        element("name", String.serializer().descriptor)
        element("indexes", IntArraySerializer().descriptor)
        element("area", AreaSerializer.descriptor)
        element("region", Int.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: MusicState) {
        val present = value.tracks.filterNotNull()
        encoder.encodeInt(value.tracks.size)
        encoder.encodeInt(present.size)
        for (track in present) {
            encoder.encodeInt(track.id)
            encoder.encodeString(track.name)
            encoder.encodeInt(track.indexes.size)
            for (index in track.indexes) {
                encoder.encodeInt(index)
            }
            encoder.encodeInt(track.area.size)
            for (area in track.area) {
                AreaSerializer.serialize(encoder, area)
            }
        }
        encoder.encodeInt(value.trackAreas.size)
        for ((region, list) in value.trackAreas) {
            encoder.encodeInt(region)
            encoder.encodeInt(list.size)
            for (areaTrack in list) {
                val track = value.tracks[areaTrack.id]!!
                encoder.encodeInt(areaTrack.id)
                encoder.encodeInt(track.area.indexOfFirst { it === areaTrack.area })
            }
        }
    }

    override fun deserialize(decoder: Decoder): MusicState {
        val tracks = arrayOfNulls<Track>(decoder.decodeInt())
        val ids = Object2IntOpenHashMap<String>(650, Hash.VERY_FAST_LOAD_FACTOR)
        repeat(decoder.decodeInt()) {
            val id = decoder.decodeInt()
            val name = decoder.decodeString()
            val indexes = IntArray(decoder.decodeInt()) { decoder.decodeInt() }.toMutableList()
            val areas = List(decoder.decodeInt()) { AreaSerializer.deserialize(decoder) }
            tracks[id] = Track(id, name, indexes, areas)
            ids[name] = id
        }
        val size = decoder.decodeInt()
        val regions = Int2ObjectOpenHashMap<MutableList<AreaTrack>>(size, Hash.VERY_FAST_LOAD_FACTOR)
        repeat(size) {
            val region = decoder.decodeInt()
            regions[region] = MutableList(decoder.decodeInt()) {
                val id = decoder.decodeInt()
                AreaTrack(id, tracks[id]!!.area[decoder.decodeInt()])
            }
        }
        return MusicState(regions, ids, tracks)
    }
}
