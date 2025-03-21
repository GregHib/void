package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinitionFull
import world.gregs.voidps.engine.data.Settings

object RemovePriceCheckerTradeLimit : InterfaceModifier() {

    fun convert(cache: CacheLibrary) {
        val priceChecker = 206
        val modifications = mutableMapOf<Int, (InterfaceComponentDefinitionFull) -> Unit>()
        // Hide middle line and limit text
        for (i in 13..15) {
            modifications[i] = { it.hidden = true }
        }
        modifications[22] = { it.hidden = true }

        // Move total into center
        modifications[21] = { it.horizontalPositionMode = 1 }
        modifyInterface(cache, priceChecker, modifications)
        cache.update()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache = CacheLibrary(Settings["storage.cache.path"])
        convert(cache)
    }
}