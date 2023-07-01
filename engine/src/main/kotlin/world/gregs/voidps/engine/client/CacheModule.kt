package world.gregs.voidps.engine.client

import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman

@Suppress("USELESS_CAST")
@Deprecated("Not in use")
val cacheModule = module {
    single(createdAtStart = true) {
        CacheDelegate(getProperty("cachePath")) as Cache
    }
    single { Huffman(get()) }
}

@Deprecated("Not in use")
val cacheDefinitionModule = module {
    single { AnimationDecoder(get()) }
    single { ClientScriptDecoder(get(), revision634 = true) }
    single { EnumDecoder(get()) }
    single { GraphicDecoder(get()) }
    single { InterfaceDecoder(get()) }
    single { ItemDecoder(get()) }
    single { NPCDecoder(get(), member = true) }
    single { ObjectDecoder(get(), member = true, lowDetail = false) }
    single { VarBitDecoder(get()) }
    single { QuickChatPhraseDecoder(get()) }
}

@Deprecated("Not in use")
val cacheConfigModule = module {
    single { ContainerDecoder(get()) }
    single { StructDecoder(get()) }
}