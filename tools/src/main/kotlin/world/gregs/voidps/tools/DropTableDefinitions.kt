package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.voidps.engine.entity.item.drop.DropTables

object DropTableDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val files = configFiles()
        val categories = CategoryDefinitions().load(files.find(Settings["definitions.categories"]))
        val ammo = AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"]))
        val parameters = ParameterDefinitions(categories, ammo).load(files.find(Settings["definitions.parameters"]))
        val itemDefinitions = ItemDefinitions.init(ItemDecoder(parameters).load(cache)).load(files.list(Settings["definitions.items"]))
        startKoin {
            modules(
                module {
                    @Suppress("USELESS_CAST")
                    single { CacheDelegate(Settings["storage.cache.path"]) as Cache }
                    single { ItemDefinitions.init(ItemDecoder().load(get())).load(listOf()) }
                },
            )
        }
        val decoder = DropTables().load(configFiles().getValue(Settings["spawns.drops"]), itemDefinitions)
//        val table = decoder.getValue("goblin_drop_table")
//
//        val list = mutableListOf<ItemDrop>()
//        repeat(1000000) {
//            table.role(list = list)
//        }
//        val inventory = Inventory.debug(capacity = 100)
//        list.forEach {
//            val item = it.toItem()
//            if (item.isNotEmpty()) {
//                inventory.add(item.id, item.amount)
//            }
//        }
//        for (item in inventory.items) {
//            if (item.isNotEmpty()) {
//                println(item)
//            }
//        }
    }
}
