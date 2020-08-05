package rs.dusk.cache

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index255
import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import rs.dusk.cache.config.decoder.*
import rs.dusk.cache.definition.decoder.*
import rs.dusk.cache.secure.Huffman
import java.math.BigInteger

@Suppress("USELESS_CAST", "RemoveExplicitTypeArguments")
val cacheModule = module {
    single(createdAtStart = true) {
        CacheDelegate(
            getProperty("cachePath"),
            getProperty<String>("fsRsaPrivate"),
            getProperty<String>("fsRsaModulus")
        ) as Cache
    }
    single { Huffman(get()) }
}

val cacheDefinitionModule = module {
    single { AnimationDecoder() }
    single { BodyDecoder() }
    single { EnumDecoder() }
    single { GraphicDecoder() }
    single { InterfaceDecoder() }
    single { ItemDecoder() }
    single { NPCDecoder(member = true) }
    single { ObjectDecoder(member = true, lowDetail = false) }
    single { QuickChatOptionDecoder() }
    single { SpriteDecoder() }
    single { TextureDecoder() }
    single { VarBitDecoder() }
    single { WorldMapDecoder() }
}

val cacheConfigModule = module {
    single { ClientVariableParameterDecoder() }
    single { HitSplatDecoder() }
    single { IdentityKitDecoder() }
    single { ItemContainerDecoder() }
    single { MapSceneDecoder() }
    single { OverlayDecoder() }
    single { PlayerVariableParameterDecoder() }
    single { QuestDecoder }
    single { RenderAnimationDecoder() }
    single { StructDecoder() }
    single { UnderlayDecoder() }
    single { WorldMapInfoDecoder() }
}

/**
 * @author Tyluur <contact@kiaira.tech>
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since January 01, 2020
 */
class CacheDelegate(directory: String, exponent: BigInteger, modulus: BigInteger) : Cache {

    private val delegate = CacheLibrary(directory)

    constructor(directory: String, exponent: String, modulus: String) : this(directory, BigInteger(exponent, 16), BigInteger(modulus, 16))

    private val logger = InlineLogger()

    private val versionTable = generateVersionTable(exponent, modulus)

    init {
        logger.info { "Cache read from $directory" }
    }

    override var index255: Index255?
        get() = delegate.index255
        set(value) {
            delegate.index255 = value
        }


    override fun getFile(index: Int, archive: Int, file: Int, xtea: IntArray?) =
        delegate.data(index, archive, file, xtea)

    override fun getFile(index: Int, name: String, xtea: IntArray?) = delegate.data(index, name, xtea)

    override fun getArchive(indexId: Int, archiveId: Int): ByteArray? {
        if (indexId == 255 && archiveId == 255) {
            return versionTable
        }
        val index = if (indexId == 255) index255 else delegate.index(indexId)
        if (index == null) {
            logger.debug { "Unable to find valid index for file request [indexId=$indexId, archiveId=$archiveId]}" }
            return null
        }
        val archiveSector = index.readArchiveSector(archiveId)
        if (archiveSector == null) {
            logger.debug { "Unable to read archive sector $archiveId in index $indexId" }
            return null
        }
        return archiveSector.data
    }

    override fun generateVersionTable(exponent: BigInteger, modulus: BigInteger) = delegate.generateNewUkeys(exponent, modulus)

    override fun close() = delegate.close()

    override fun getIndexCrc(indexId: Int): Int {
        return delegate.index(indexId).crc
    }

    override fun lastIndexId(indexId: Int): Int {
        return delegate.index(indexId).last()?.id ?: 0
    }

    override fun archiveCount(indexId: Int, archiveId: Int): Int {
        return delegate.index(indexId).archive(archiveId)?.fileIds()?.size ?: 0
    }

    override fun lastFileId(indexId: Int, archive: Int): Int {
        return delegate.index(indexId).archive(archive)?.last()?.id ?: -1
    }

    override fun lastArchiveId(indexId: Int): Int {
        return delegate.index(indexId).last()?.id ?: -1
    }

    override fun getArchiveId(index: Int, name: String): Int {
        return delegate.index(index).archiveId(name)
    }
}