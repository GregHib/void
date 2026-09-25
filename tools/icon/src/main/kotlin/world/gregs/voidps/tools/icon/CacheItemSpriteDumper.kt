package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.FileCache
import java.io.File

/**
 * Standalone entry point: dumps every item's inventory icon straight from a
 * local cache directory, without booting the applet/login/game-loop machinery.
 *
 *
 * Usage: `java CacheItemSpriteDumper <cache directory> [output directory]`
 *
 *
 * &lt;cache directory&gt; must contain the classic flat cache files
 * (main_file_cache.dat2, main_file_cache.idx0 .. idx255). Item icons read
 * models, item definitions, billboards and the material/texture/sprite
 * archives the texture manager needs.
 */
object CacheItemSpriteDumper {

    @JvmStatic
    fun main(args: Array<String>) {
        val cacheDir = if (args.size > 0) args[0] else "C:\\Users\\Greg\\IdeaProjects\\void\\data\\cache\\"

        try {
            val cache: Cache = FileCache(cacheDir)
            Class73.cache = cache
            Class348_Sub40_Sub4.aTextureSource9113 = Js5TextureSource(cache)
            ItemSpriteDumper.itemTypeList = ItemTypeList(0, cache)
        } catch (exception: RuntimeException) {
            System.err.println("Failed to load cache from " + cacheDir)
            exception.printStackTrace()
            System.exit(1)
            return
        }

        val outDir = File(if (args.size > 1) args[1] else "item_sprites")
        ItemSpriteDumper.dump(outDir)
    }
}
