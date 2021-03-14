package world.gregs.voidps.network

import org.koin.dsl.module

val networkCodecs = module {
    single(createdAtStart = true) { GameCodec() }
}