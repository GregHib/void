package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.add

object DropTableDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load("game.properties")
        startKoin {
            modules(module {
                @Suppress("USELESS_CAST")
                single { CacheDelegate(Settings["storage.cache.path"]) as Cache }
                single { ItemDefinitions(ItemDecoder().load(get())).load(listOf()) }
            })
        }
        val decoder = DropTables().load(configFiles().getValue(Settings["spawns.drops"]))
        val table = decoder.getValue("goblin_drop_table")

        val list = mutableListOf<ItemDrop>()
        repeat(1000000) {
            table.role(list = list, members = true)
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