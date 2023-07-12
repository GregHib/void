package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.contain.Inventory
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.yaml.Yaml

object DropTableDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(module {
                single { CacheDelegate(getProperty("cachePath")) as Cache }
                single { ItemDefinitions(ItemDecoder().loadCache(get())).load(Yaml()) }
            })
        }.koin
        val decoder = DropTables().load(Yaml())
        val table = decoder.getValue("goblin_drop_table")

        val list = mutableListOf<ItemDrop>()
        repeat(1000000) {
            table.role(list = list)
        }
        val inventory = Inventory.debug(capacity = 100)
        list.forEach {
            val item = it.toItem()
            if (item.isNotEmpty()) {
                inventory.add(item.id, item.amount)
            }
        }
        for (item in inventory.items) {
            if (item.isNotEmpty()) {
                println(item)
            }
        }
    }
}