package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest

class SettingsReloadTest : ScriptTest {

    override val checks = listOf(
        listOf<String>(),
    )
    override val failedChecks = emptyList<List<String>>()

    override fun Script.register(args: List<String>, caller: Caller) {
        settingsReload {
            caller.call()
        }
    }

    override fun invoke(args: List<String>) {
        SettingsReload.now()
    }

    override val apis = listOf(SettingsReload)

}