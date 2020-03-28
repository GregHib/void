package org.redrune.engine.script.koin

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.test.mock.MockProvider
import org.koin.test.mock.Provider

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class MockProviderExtension private constructor(private val mockProvider: Provider<*>) : BeforeEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        MockProvider.register(mockProvider)
    }

    companion object {
        fun create(mockProvider: Provider<*>): MockProviderExtension {
            return MockProviderExtension(mockProvider)
        }
    }
}