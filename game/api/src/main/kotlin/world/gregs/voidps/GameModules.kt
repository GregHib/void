package world.gregs.voidps

import org.koin.dsl.module
import world.gregs.voidps.world.activity.quest.Books
import world.gregs.voidps.world.interact.entity.obj.Teleports
import world.gregs.voidps.world.interact.entity.player.music.MusicTracks
import world.gregs.voidps.world.interact.world.spawn.ItemSpawns

val gameModule = module {
    single { ItemSpawns() }
    single(createdAtStart = true) { Books().load() }
    single(createdAtStart = true) { MusicTracks().load() }
    single(createdAtStart = true) { Teleports().load() }
}