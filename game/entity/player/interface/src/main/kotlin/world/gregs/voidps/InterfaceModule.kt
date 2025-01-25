package world.gregs.voidps

import org.koin.dsl.module
import world.gregs.voidps.world.activity.quest.Books

val interfaceModule = module {
    single(createdAtStart = true) { Books().load() }
}