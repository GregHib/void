package world.gregs.voidps.engine.script.koin

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.KoinAppDeclaration

class KoinTestExtension private constructor(private val appDeclaration: KoinAppDeclaration) :
    BeforeEachCallback,
    AfterEachCallback {

    private var _koin: Koin? = null
    val koin: Koin
        get() = _koin ?: error("No Koin application found")

    override fun beforeEach(context: ExtensionContext) {
        _koin = startKoin(appDeclaration = appDeclaration).koin
    }

    override fun afterEach(context: ExtensionContext?) {
        stopKoin()
        _koin = null
    }

    companion object {
        fun create(appDeclaration: KoinAppDeclaration): KoinTestExtension = KoinTestExtension(appDeclaration)
    }
}
