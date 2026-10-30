package world.gregs.voidps.tools.avatar

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.file.FileStorage
import java.awt.image.BufferedImage
import java.io.File

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
 * Options: --out=<dir> (default ./data/avatars), --size=<px> (default 192), --yaw=<deg>/--pitch=<deg> (chathead angle),
 *  --chat-anim=<id>/--chat-frame=<n> (chathead expression, default 9807 frame 0).
 * Player modes read file saves (storage.players.path); for database storage use the web server's avatar endpoints.
 *
 * Gradle: ./gradlew :tools:avatar:renderPhotoBooth -Pargs="--player=name --out=/tmp/avatar"
 */
object PhotoBoothRenderer {

    @JvmStatic
    fun main(args: Array<String>) {
        val outDir = File(args.value("--out") ?: "./data/avatars").apply { mkdirs() }
        val size = args.value("--size")?.toIntOrNull() ?: 192

        Settings.load("./game/src/main/resources/game.properties")
        val cache = CacheDelegate(Settings["storage.cache.path"])
        // ItemDecoder (not Full) drives ItemDefinitions and assigns the same sequential equipIndex the
        // game uses, so the equipIndex->itemId map is guaranteed to match captured snapshots.
        ItemDefinitions.init(ItemDecoder().load(cache)).load(configFiles().list(Settings["definitions.items"]))
        val booth = PhotoBooth(
            cache,
            chatAnimation = args.value("--chat-anim")?.toIntOrNull() ?: PhotoBooth.DEFAULT_CHAT_ANIMATION,
            chatFrame = args.value("--chat-frame")?.toIntOrNull() ?: 0,
            chatYaw = args.value("--yaw")?.toDoubleOrNull() ?: -25.0,
            chatPitch = args.value("--pitch")?.toDoubleOrNull() ?: 8.0,
        )

        fun writePng(image: BufferedImage?, type: String, name: String) {
            if (image == null) return
            val file = PhotoBooth.file(outDir, name, type)
            PhotoBooth.write(image, file)
            println("  ✓ $name [$type] -> ${file.path}")
        }

        fun render(snapshot: PhotoSnapshot, name: String) {
            val avatar = booth.render(snapshot, size)
            if (avatar == null) {
                println("  ! $name: no renderable model (empty snapshot?)")
                return
            }
            writePng(avatar.full, PhotoBooth.FULL, name)
            writePng(avatar.chat, PhotoBooth.CHAT, name)
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
        block(SnapshotRepository(FileStorage(File(Settings["storage.players.path"]))))
    }

    /** Parses `--snapshot=male;looks;colours;equipment`, e.g. `true;6,98,452,105,34,627,434;3,189,189,39,0;-1,...`. */
    private fun parseLiteral(value: String): PhotoSnapshot {
        val parts = value.split(";")
        require(parts.size >= 4) { "--snapshot must be male;looks;colours;equipment" }
        return PhotoSnapshot.parse(parts[0].trim().toBoolean(), parts[1], parts[2], parts[3], 0L)
    }

    private fun Array<String>.value(prefix: String): String? = firstOrNull { it.startsWith("$prefix=") }?.substringAfter("=")
}
