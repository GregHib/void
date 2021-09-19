package world.gregs.voidps.world.script

import io.mockk.every
import io.mockk.mockk
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.config.data.ContainerDefinition
import world.gregs.voidps.cache.config.decoder.*
import world.gregs.voidps.cache.definition.data.*
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.engine.data.StorageStrategy
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Provides a complete mock of all cache and cache decoding calls for [WorldMock] tests
 */

val mockCacheModule = module {
    single(createdAtStart = true) {
        mockk<Cache>(relaxed = true) {
            every { getFile(any(), archive = any(), file = any()) } returns null
            every { getFile(any(), name = any(), xtea = any()) } returns null
        }
    }
}

val mockJsonPlayerModule = module {
    single {
        mockk<StorageStrategy<Player>>(relaxed = true)
    }
}

val mockCacheDefinitionModule = module {
    single { mockDef<AnimationDecoder, AnimationDefinition> { AnimationDefinition(it) } }
    single { mockk<BodyDecoder>() }
    single {
        mockDef<ClientScriptDecoder, ClientScriptDefinition> { ClientScriptDefinition(it) }
    }
    single {
        mockDef<EnumDecoder, EnumDefinition> { EnumDefinition(id = it, map = HashMap()) }
    }
    single { mockk<GraphicDecoder>() }
    single {
        mockDef<InterfaceDecoder, InterfaceDefinition> { id -> InterfaceDefinition(id = id, components = (0..20).associateWith { InterfaceComponentDefinition(id = it) }) }
    }
    single {
        mockk<ItemDecoder> {
            every { getOrNull(any()) } answers { ItemDefinition(id = arg(0)) }
            every { get(any<Int>()) } answers { ItemDefinition(id = arg(0)) }
            every { last } returns Short.MAX_VALUE.toInt()
            every { get(995) } returns ItemDefinition(
                id = 995,
                name = "Coins",
                stackable = 1
            )
        }
    }
    single {
        mockk<NPCDecoder> {
            every { getOrNull(any()) } answers { NPCDefinition(id = arg(0)) }
            every { get(any<Int>()) } answers { NPCDefinition(id = arg(0)) }
            every { get(47) } returns NPCDefinition(
                id = 47,
                name = "Rat",
                options = arrayOf(null, "Attack", null, null, null, "Examine")
            )
        }
    }
    single {
        mockDef<ObjectDecoder, ObjectDefinition> { ObjectDefinition(id = it) }
    }
    single { mockk<QuickChatOptionDecoder>() }
    single { mockk<SpriteDecoder>() }
    single { mockk<TextureDecoder>() }
    single { mockk<VarBitDecoder>() }
    single { mockk<WorldMapDetailsDecoder>() }
    single { mockk<WorldMapIconDecoder>() }
}

private inline fun <reified Decoder : DefinitionDecoder<Def>, Def : Definition> mockDef(crossinline create: (Int) -> Def): Decoder = mockk {
    every { getOrNull(any()) } answers { create(arg(0)) }
    every { get(any<Int>()) } answers { create(arg(0)) }
}

val mockCacheConfigModule = module {
    single { mockk<ClientVariableParameterDecoder>() }
    single { mockk<HitSplatDecoder>() }
    single { mockk<IdentityKitDecoder>() }
    single {
        mockk<ContainerDecoder> {
            every { get(any<Int>()) } answers { ContainerDefinition(id = arg(0)) }
            every { get(93) } returns ContainerDefinition(id = 93, length = 28)
            every { get(94) } returns ContainerDefinition(id = 94, length = 15)
            every { get(90) } returns ContainerDefinition(id = 90, length = 28)
        }
    }
    single { mockk<MapSceneDecoder>() }
    single { mockk<OverlayDecoder>() }
    single { mockk<PlayerVariableParameterDecoder>() }
    single { mockk<QuestDecoder>() }
    single { mockk<RenderAnimationDecoder>() }
    single { mockk<StructDecoder>() }
    single { mockk<UnderlayDecoder>() }
    single { mockk<WorldMapInfoDecoder>() }
}