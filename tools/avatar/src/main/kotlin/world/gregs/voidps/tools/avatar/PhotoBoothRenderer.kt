package world.gregs.voidps.tools.avatar

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.IdentityKitDecoder
import world.gregs.voidps.cache.config.decoder.RenderAnimationDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.storage.DatabaseStorage
import world.gregs.voidps.tools.render.Class348_Sub40_Sub4
import world.gregs.voidps.tools.render.Class73
import world.gregs.voidps.tools.render.Js5TextureSource
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Renders photo booth avatars from captured snapshots using the client's software renderer.
 * For each player it writes two transparent PNGs, mirroring Jagex's avatar service:
 * `<name>_full.png` (full body) and `<name>_chat.png` (chathead; falls back to a head crop of the
 * body when no item or kit has a chathead model).
 *
 * Modes (one required):
 *  --snapshot=<male>;<looks>;<colours>;<equipment>  DB-less render of a literal snapshot (for testing)
 *  --player=<accountName>                           render one player's stored snapshot
 *  --players=<name,name,...>                        render several players (e.g. the dirty set)
 *  --all-dirty                                      render every player flagged photo_booth_dirty
 *
 * Options: --out=<dir> (default ./data/avatars), --size=<px> (default 192), --yaw=<deg>/--pitch=<deg> (chathead angle).
 * DB modes require storage.database.* in game.properties.
 *
 * Gradle: ./gradlew :tools:avatar:renderPhotoBooth -Pargs="--player=name --out=/tmp/avatar"
 */
object PhotoBoothRenderer {

    private const val DEFAULT_RENDER_EMOTE = 1426

    @JvmStatic
    fun main(args: Array<String>) {
        val outDir = File(args.value("--out") ?: "./data/avatars").apply { mkdirs() }
        val size = args.value("--size")?.toIntOrNull() ?: 192

        Settings.load()
        val cache = CacheDelegate(Settings["storage.cache.path"])
        val files = configFiles()
        Class73.cache = cache
        Class348_Sub40_Sub4.aTextureSource9113 = Js5TextureSource(cache)

        // ItemDecoder (not Full) drives ItemDefinitions and assigns the same sequential equipIndex the
        // game uses, so the equipIndex->itemId map is guaranteed to match captured snapshots.
        ItemDefinitions.init(ItemDecoder().load(cache)).load(files.list(Settings["definitions.items"]))
        val equipIndexToItemId = HashMap<Int, Int>(8192)
        ItemDefinitions.definitions.forEachIndexed { id, def ->
            if (def.equipIndex != -1) equipIndexToItemId[def.equipIndex] = id
        }

        val assembler = PlayerMeshAssembler(
            cache = cache,
            equipIndexToItemId = equipIndexToItemId,
            items = ItemDecoderFull().load(cache),
            identityKits = IdentityKitDecoder().load(cache),
            renderAnimations = RenderAnimationDecoder().load(cache),
            itemType = { itemId -> ItemDefinitions.definitions.getOrNull(itemId)?.type ?: EquipType.None },
            renderEmote = { itemId -> ItemDefinitions.definitions.getOrNull(itemId)?.get("render_emote", DEFAULT_RENDER_EMOTE) ?: DEFAULT_RENDER_EMOTE },
        )
        val renderer = AvatarRenderer(Class348_Sub40_Sub4.aTextureSource9113)
        // Chathead is turned to a 3/4 view (and tilted slightly) to match RS forum avatars
        val chatYaw = args.value("--yaw")?.toDoubleOrNull() ?: -25.0
        val chatPitch = args.value("--pitch")?.toDoubleOrNull() ?: 8.0

        fun writePng(image: BufferedImage, suffix: String, name: String) {
            val file = File(outDir, "${sanitize(name)}_$suffix.png")
            val tmp = File(outDir, "${sanitize(name)}_$suffix.png.tmp")
            ImageIO.write(image, "png", tmp)
            file.delete()
            tmp.renameTo(file)
            println("  ✓ $name [$suffix] -> ${file.path}")
        }

        fun render(snapshot: PhotoSnapshot, name: String) {
            val body = assembler.body(snapshot)
            val head = assembler.head(snapshot)
            if (body == null && head == null) {
                println("  ! $name: no renderable model (empty snapshot?)")
                return
            }
            if (body != null) {
                writePng(renderer.render(body, size, ambient = 64, contrast = 850, pitch = 10.0), "full", name)
            }
            when {
                head != null -> writePng(renderer.render(head, size, ambient = 64, contrast = 768, pitch = chatPitch, yaw = chatYaw), "chat", name)
                // No dedicated chathead mesh (e.g. full helm): crop the head from the body model.
                body != null -> writePng(renderer.renderHeadCrop(assembler.body(snapshot)!!, size, ambient = 64, contrast = 850, pitch = chatPitch, yaw = chatYaw), "chat", name)
            }
        }

        val literal = args.value("--snapshot")
        when {
            literal != null -> render(parseLiteral(literal), "snapshot")
            args.value("--player") != null -> withStorage { repo ->
                // '+' stands in for spaces so account names survive -Pargs space-splitting.
                val name = args.value("--player")!!.replace('+', ' ')
                val snapshot = repo.load(name)
                if (snapshot == null) println("No snapshot for $name") else render(snapshot, name)
            }
            args.value("--players") != null -> withStorage { repo ->
                args.value("--players")!!.split(",").map { it.trim().replace('+', ' ') }.filter { it.isNotEmpty() }.forEach { name ->
                    val snapshot = repo.load(name)
                    if (snapshot == null) println("  - $name: no snapshot") else render(snapshot, name)
                }
            }
            args.contains("--all-dirty") -> withStorage { repo ->
                val names = repo.names()
                println("Scanning ${names.size} accounts for photo_booth_dirty...")
                var rendered = 0
                for (name in names) {
                    val snapshot = repo.loadIfDirty(name) ?: continue
                    render(snapshot, name)
                    rendered++
                }
                println("Rendered $rendered dirty avatar(s)")
            }
            else -> println("No mode given. Use --snapshot=, --player=, --players= or --all-dirty. See file header.")
        }
    }

    private inline fun withStorage(block: (SnapshotRepository) -> Unit) {
        val storage: Storage = if (Settings["storage.type", "files"] == "database") {
            DatabaseStorage.connect(
                Settings["storage.database.username"],
                Settings["storage.database.password"],
                Settings["storage.database.driver"],
                Settings["storage.database.jdbcUrl"],
                Settings["storage.database.poolSize", 2],
            )
            DatabaseStorage()
        } else {
            FileStorage(File(Settings["storage.players.path"]))
        }
        block(SnapshotRepository(storage))
    }

    /** Parses `--snapshot=male;looks;colours;equipment`, e.g. `true;6,98,452,105,34,627,434;3,189,189,39,0;-1,...`. */
    private fun parseLiteral(value: String): PhotoSnapshot {
        val parts = value.split(";")
        require(parts.size >= 4) { "--snapshot must be male;looks;colours;equipment" }
        return PhotoSnapshot.parse(parts[0].trim().toBoolean(), parts[1], parts[2], parts[3], 0L)
    }

    private fun sanitize(name: String): String = name.lowercase().replace(Regex("[^a-z0-9_-]"), "_")

    private fun Array<String>.value(prefix: String): String? = firstOrNull { it.startsWith("$prefix=") }?.substringAfter("=")
}
