import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.play
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.bot.isBot
import world.gregs.voidps.world.interact.world.map.MusicTracks

val tracks: MusicTracks by inject()

on<Registered>({ !it.isBot }) { player: Player ->
    val tracks = tracks[player.tile.region]
    for (track in tracks) {
        if (track.area.contains(player.tile)) {
            player.play(musicId(track.index))
        }
    }
}

on<Moved>({ !it.isBot }) { player: Player ->
    val tracks = tracks[player.tile.region]
    for (track in tracks) {
        if (!track.area.contains(from) && track.area.contains(to)) {
            player.play(musicId(track.index))
        }
    }
}

val enumDefs: EnumDecoder by inject()

fun musicId(index: Int): Int = enumDefs.get(1351).getInt(index)

fun musicName(index: Int): Int = enumDefs.get(1351).getInt(index)

fun musicHint(index: Int): Int = enumDefs.get(1349).getInt(index)