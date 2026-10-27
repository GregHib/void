package content.entity.world.music

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat

object MusicTrackLoader : Loader<MusicState> {
    val instance = MusicTracks()
    override val name = "music_tracks"
    override val label = "music track"
    override val file = listOf("map.music")
    override val codec = SnapshotFormat.codec(MusicStateSerializer)

    override fun load(context: LoadContext): MusicState = instance.parse(context.paths(this).single())

    override fun total(state: MusicState) = state.ids.size

    override fun install(state: MusicState) = instance.install(state)
}
