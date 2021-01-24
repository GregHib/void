package world.gregs.voidps.engine.client

import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.*
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.cache.secure.Huffman

@Suppress("USELESS_CAST", "RemoveExplicitTypeArguments")
val cacheModule = module {
    single(createdAtStart = true) {
        CacheDelegate(getProperty("cachePath")) as Cache
    }
    single { Huffman(get()) }
}
val cacheDefinitionModule = module {
    single { AnimationDecoder(get()) }
    single { BodyDecoder(get()) }
    single { ClientScriptDecoder(get()) }
    single { EnumDecoder(get()) }
    single { GraphicDecoder(get()) }
    single { InterfaceDecoder(get()) }
    single { ItemDecoder(get()) }
    single { NPCDecoder(get(), member = true) }
    single { ObjectDecoder(get(), member = true, lowDetail = false, configReplace = true) }
    single { QuickChatOptionDecoder(get()) }
    single { SpriteDecoder(get()) }
    single { TextureDecoder(get()) }
    single { VarBitDecoder(get()) }
    single { WorldMapDetailsDecoder(get()) }
    single { WorldMapIconDecoder(get()) }
}
val cacheConfigModule = module {
    single { ClientVariableParameterDecoder(get()) }
    single { HitSplatDecoder(get()) }
    single { IdentityKitDecoder(get()) }
    single { ContainerDecoder(get()) }
    single { MapSceneDecoder(get()) }
    single { OverlayDecoder(get()) }
    single { PlayerVariableParameterDecoder(get()) }
    single { QuestDecoder(get()) }
    single { RenderAnimationDecoder(get()) }
    single { StructDecoder(get()) }
    single { UnderlayDecoder(get()) }
    single { WorldMapInfoDecoder(get()) }
}