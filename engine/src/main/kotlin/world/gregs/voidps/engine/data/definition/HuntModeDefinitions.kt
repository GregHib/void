package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.HuntModeDefinition
import world.gregs.voidps.engine.timedLoad

class HuntModeDefinitions {

    private lateinit var modes: Map<String, HuntModeDefinition>

    fun get(name: String): HuntModeDefinition {
        return modes.getValue(name)
    }

    fun load(path: String = Settings["definitions.huntModes"]): HuntModeDefinitions {
        timedLoad("hunt mode") {
            val modes = Object2ObjectOpenHashMap<String, HuntModeDefinition>(10, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path) {
                while (nextSection()) {
                    val mode = section()
                    var type = ""
                    var checkVisual = "none"
                    var checkNotTooStrong = false
                    var checkNotCombat = true
                    var checkNotCombatSelf = true
                    var checkNotBusy = true
                    var checkAfk = false
                    var findKeepHunting = false
                    var pauseIfNobodyNear = true
                    var rate: Int? = null
                    var id = ""
                    var layer: Int = -1
                    var maxMultiAttackers = 2
                    while (nextPair()) {
                        when (val key = key()) {
                            "type" -> type = string()
                            "check_visual" -> checkVisual = string()
                            "check_not_too_strong" -> checkNotTooStrong = boolean()
                            "check_not_combat" -> checkNotCombat = boolean()
                            "check_not_combat_self" -> checkNotCombatSelf = boolean()
                            "check_not_busy" -> checkNotBusy = boolean()
                            "check_afk" -> checkAfk = boolean()
                            "find_keep_hunting" -> findKeepHunting = boolean()
                            "pause_if_nobody_near" -> pauseIfNobodyNear = boolean()
                            "rate" -> rate = int()
                            "id" -> id = string()
                            "layer" -> layer = int()
                            "max_multi_attackers" -> maxMultiAttackers = int()
                            else -> throw IllegalArgumentException("Unknown hunt mode key: $key")
                        }
                    }
                    modes[mode] = HuntModeDefinition(
                        type,
                        checkVisual,
                        checkNotTooStrong,
                        checkNotCombat,
                        checkNotCombatSelf,
                        checkNotBusy,
                        checkAfk,
                        findKeepHunting,
                        pauseIfNobodyNear,
                        rate ?: if (type == "player") 1 else 3,
                        id,
                        layer,
                        maxMultiAttackers
                    )
                }
            }
            this.modes = modes
            modes.size
        }
        return this
    }

}