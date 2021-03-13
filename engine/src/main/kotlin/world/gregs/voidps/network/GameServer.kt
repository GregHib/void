package world.gregs.voidps.network

import org.koin.dsl.module
import world.gregs.voidps.network.codec.game.GameCodec

val networkCodecs = module {
    single(createdAtStart = true) { GameCodec().apply { run() } }
}