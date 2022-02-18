package world.gregs.voidps.engine.client

import org.koin.dsl.module
import world.gregs.voidps.engine.utility.getIntProperty

val clientConnectionModule = module {
    single {
        ConnectionQueue(getIntProperty("connectionPerTickCap", 1))
    }
    single { ConnectionGatekeeper(get()) }
}