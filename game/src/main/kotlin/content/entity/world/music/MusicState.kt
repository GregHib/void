package content.entity.world.music

class MusicState(
    val trackAreas: Map<Int, List<AreaTrack>>,
    val ids: Map<String, Int>,
    val tracks: Array<Track?>,
)
