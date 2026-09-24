package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import java.io.File
import java.io.IOException

/**
 * Standalone entry point: dumps every item's inventory icon straight from a
 * local cache directory, without booting the applet/login/game-loop machinery.
 * 
 * 
 * Usage: `java CacheItemSpriteDumper <cache directory> [output directory]`
 * 
 * 
 * &lt;cache directory&gt; must contain the classic flat cache files
 * (main_file_cache.dat2, main_file_cache.idx0 .. idx255). Only the archives
 * actually needed to build and render item icons are loaded: models (7),
 * item definitions (19), and the three archives the material/texture manager
 * needs (materials 26, texture data 9, texture sprites 8), plus fonts (31)
 * since the off-screen renderer expects one.
 */
object CacheItemSpriteDumper {
    private const val IDX_MODELS = 7
    private const val IDX_TEXTURE_SPRITES = 8
    private const val IDX_TEXTURE_DATA = 9
    private const val IDX_ITEMS = 19
    private const val IDX_MATERIALS = 26
    private const val IDX_BILLBOARD = 29
    private const val IDX_FONTS = 31

    @JvmStatic
    fun main(args: Array<String>) {
        val cacheDir = if (args.size > 0) args[0] else "C:\\Users\\Greg\\IdeaProjects\\void\\data\\cache\\"

        val cache: Cache = CacheDelegate(cacheDir, null, null)
        try {
            println(cache.versionTable.contentToString())
            println(cache.data(0, 0, 26, null).contentToString())
            aa_Sub3.Companion.aJs5_5207 = loadArchive(cacheDir, cache, IDX_MODELS, false)
            Class174.aJs5_2306 = loadArchive(cacheDir, cache, IDX_ITEMS, false)
            Class348_Sub11.aJs5_4770 = loadArchive(cacheDir, cache, IDX_MATERIALS, true)
            Class369_Sub2.aJs5_8589 = loadArchive(cacheDir, cache, IDX_TEXTURE_DATA, false)
            Class21.aJs5_322 = loadArchive(cacheDir, cache, IDX_TEXTURE_SPRITES, false)
            Class136.aJs5_4796 = loadArchive(cacheDir, cache, IDX_FONTS, true)
            Class369_Sub3.aJs5_8601 = loadArchive(cacheDir, cache, IDX_BILLBOARD, false)

            Class348_Sub40_Sub4.Companion.aTextureSource9113 = Js5TextureSource(Class348_Sub11.aJs5_4770, Class369_Sub2.aJs5_8589, Class21.aJs5_322)
            Exception_Sub1.itemTypeList = ItemTypeList(0, true, null, Class174.aJs5_2306, aa_Sub3.Companion.aJs5_5207)
        } catch (exception: IOException) {
            System.err.println("Failed to load cache from " + cacheDir)
            exception.printStackTrace()
            System.exit(1)
            return
        } catch (exception: RuntimeException) {
            System.err.println("Failed to load cache from " + cacheDir)
            exception.printStackTrace()
            System.exit(1)
            return
        }

        val outDir = File(if (args.size > 1) args[1] else "item_sprites")
        ItemSpriteDumper.dump(outDir)
    }

    @Throws(IOException::class)
    private fun loadArchive(cacheDir: String?, cache: Cache?, index: Int, unpackEagerly: Boolean): Js5 {
        return Js5(DiskFileStore(cacheDir, index, cache), unpackEagerly, 1)
    }
}
