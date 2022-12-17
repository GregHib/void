package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop

object DropTableDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, module {
                single { ItemDefinitions(ItemDecoder(get())).load(FileStorage()) }
            })
        }.koin
        val decoder = DropTables().load(FileStorage())
        val table = decoder.getValue("goblin_drop_table")

        val list = mutableListOf<ItemDrop>()
        repeat(1000000) {
            table.role(list = list)
        }
        val container = Container.debug(capacity = 100)
        list.forEach {
            val item = it.toItem()
            if (item.isNotEmpty()) {
                container.add(item.id, item.amount)
            }
        }
        for (item in container.getItems()) {
            if (item.isNotEmpty()) {
                println(item)
            }
        }
    }
}