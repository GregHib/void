package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.config.decoder.StructDecoder
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions

object EnumDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val files = configFiles()
        ItemDefinitions.init(ItemDecoder().load(cache)).load(files.list(Settings["definitions.items"]))
        InterfaceDefinitions.init(InterfaceDecoder().load(cache)).load(files.list(Settings["definitions.interfaces"]), files.find(Settings["definitions.interfaces.types"]))
        InventoryDefinitions.init(InventoryDecoder().load(cache)).load(files.list(Settings["definitions.inventories"]), files.list(Settings["definitions.shops"]))
        NPCDefinitions.init(NPCDecoder().load(cache)).load(files.list(Settings["definitions.npcs"]))
        StructDefinitions.init(StructDecoder().load(cache)).load(files.find(Settings["definitions.structs"]))
        val definitions = EnumDefinitions.init(EnumDecoder().load(cache)).load(files.list(Settings["definitions.enums"]))
        for (i in definitions.definitions.indices) {
            val def = definitions.getOrNull(i) ?: continue
            println("$i $def")
        }
    }
}
