package world.gregs.voidps

import org.koin.dsl.module
import world.gregs.voidps.world.interact.entity.obj.Teleports
import world.gregs.voidps.world.interact.entity.player.music.MusicTracks

val gameModule = module {
    single(createdAtStart = true) { MusicTracks().load() }
    single(createdAtStart = true) { Teleports().load() }
}