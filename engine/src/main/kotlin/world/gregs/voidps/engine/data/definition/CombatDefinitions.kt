package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.config.CombatDefinition
import world.gregs.voidps.engine.data.config.CombatDefinition.Hit
import world.gregs.voidps.engine.data.config.CombatDefinition.Projectile
import world.gregs.voidps.engine.timedLoad

class CombatDefinitions {

    private lateinit var definitions: Map<String, List<CombatDefinition>>

    fun get(key: String) = definitions[key] ?: CombatDefinition("", "", 0)

    fun load(path: String): CombatDefinitions {
        timedLoad("combat definition") {
            val definitions = Object2ObjectOpenHashMap<String, MutableList<CombatDefinition>>()
            Config.fileReader(path) {
                while (nextSection()) {
                    val (stringId, id) = section().split(".")
                    var chance = 0
                    var range = 1
                    var condition = ""
                    var anim = ""
                    var gfx = ""
                    var areaGfx: CombatDefinition.AreaGfx? = null
                    var sound = ""
                    var areaSound: CombatDefinition.AreaSound? = null
                    var style = ""
                    val projectiles = mutableListOf<Projectile>()
                    var targetGfx = ""
                    var targetAnim = ""
                    val targetSounds = mutableListOf<String>()
                    var targetHit: Hit? = null
                    var impactAnim = ""
                    var missGfx = ""
                    var impactGfx = ""
                    var impactAreaGfx: CombatDefinition.AreaGfx? = null
                    var missSound = ""
                    var impactSound = ""
                    var impactAreaSound: CombatDefinition.AreaSound? = null
                    val drainSkills = mutableListOf<CombatDefinition.Drain>()
                    var freeze = 0
                    var poison = 0
                    var message = ""
                    while (nextPair()) {
                        when (val key = key()) {
                            "clone" -> throw UnsupportedOperationException("Clone not supported for combat definitions.")
                            "chance" -> chance = int()
                            "range" -> range = int()
                            "condition" -> condition = string()
                            "anim" -> anim = string()
                            "gfx" -> gfx = string()
                            "area_gfx" -> areaGfx = areaGfx()
                            "sound" -> sound = string()
                            "area_sound" -> areaSound = areaSound()
                            "style" -> style = string()
                            "projectile" -> projectiles.add(Projectile(string()))
                            "projectiles" -> {
                                while (nextElement()) {
                                    var id = ""
                                    var delay: Int? = null
                                    var curve: Int? = null
                                    var endHeight: Int? = null
                                    while (nextEntry()) {
                                        when (val key = key()) {
                                            "id" -> id = string()
                                            "delay" -> delay = int()
                                            "curve" -> curve = int()
                                            "end_height" -> endHeight = int()
                                            else -> throw IllegalArgumentException("Unknown key '$key' in projectile definition.")
                                        }
                                    }
                                    projectiles.add(Projectile(id = id, delay = delay, curve = curve, endHeight = endHeight))
                                }
                            }
                            "target_gfx" -> targetGfx = string()
                            "target_anim" -> targetAnim = string()
                            "target_sound" -> targetSounds.add(string())
                            "target_sounds" -> while (nextElement()) {
                                targetSounds.add(string())
                            }
                            "target_hit" -> {
                                var offense = ""
                                var defence: String? = null
                                var spell = ""
                                var special = false
                                var min = 0
                                var max = 0
                                while (nextEntry()) {
                                    when (val key = key()) {
                                        "offense" -> offense = string()
                                        "defence" -> defence = string()
                                        "spell" -> spell = string()
                                        "special" -> special = boolean()
                                        "min" -> min = int()
                                        "max" -> max = int()
                                        else -> throw IllegalArgumentException("Unknown key '$key' in hit definition.")
                                    }
                                }
                                targetHit = Hit(offense, defence ?: offense, spell, special, min, max)
                            }
                            "impact_anim" -> impactAnim = string()
                            "miss_gfx" -> missGfx = string()
                            "impact_gfx" -> impactGfx = string()
                            "impact_area_gfx" -> impactAreaGfx = areaGfx()
                            "miss_sound" -> missSound = string()
                            "impact_sound" -> impactSound = string()
                            "impact_area_sound" -> impactAreaSound = areaSound()
                            "impact_drain" -> while (nextElement()) {
                                var skill = ""
                                var amount = 0
                                var multiplier = 0.0
                                while (nextEntry()) {
                                    when (val key = key()) {
                                        "skill" -> skill = string()
                                        "amount" -> amount = int()
                                        "multiplier" -> multiplier = double()
                                        else -> throw IllegalArgumentException("Unknown key '$key' in hit definition.")
                                    }
                                }
                                drainSkills.add(CombatDefinition.Drain(skill, amount, multiplier))
                            }
                            "freeze" -> freeze = int()
                            "poison" -> poison = int()
                            "message" -> message = string()
                            else -> throw UnsupportedOperationException("Unknown key '$key' in combat definition.")
                        }
                    }
                    definitions.getOrPut(stringId) { mutableListOf() }.add(
                        CombatDefinition(
                            npc = stringId,
                            id = id,
                            chance = chance,
                            range = range,
                            condition = condition,
                            anim = anim,
                            gfx = gfx,
                            areaGfx = areaGfx,
                            sound = sound,
                            areaSound = areaSound,
                            style = style,
                            projectiles = projectiles,
                            targetGfx = targetGfx,
                            targetAnim = targetAnim,
                            targetSounds = targetSounds,
                            targetHit = targetHit,
                            impactAnim = impactAnim,
                            missGfx = missGfx,
                            impactGfx = impactGfx,
                            impactAreaGfx = impactAreaGfx,
                            missSound = missSound,
                            impactSound = impactSound,
                            impactAreaSound = impactAreaSound,
                            drainSkills = drainSkills,
                            freeze = freeze,
                            poison = poison,
                            message = message,
                        )
                    )
                }
            }
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

    private fun ConfigReader.areaSound(): CombatDefinition.AreaSound {
        var id = ""
        var delay = 0
        var radius = 0
        while (nextEntry()) {
            when (val key = key()) {
                "id" -> id = string()
                "delay" -> delay = int()
                "radius" -> radius = int()
                else -> throw IllegalArgumentException("Unknown key '$key' in area sound definition.")
            }
        }
        return CombatDefinition.AreaSound(id, delay, radius)
    }

    private fun ConfigReader.areaGfx(): CombatDefinition.AreaGfx {
        var id = ""
        var delay = 0
        while (nextEntry()) {
            when (val key = key()) {
                "id" -> id = string()
                "delay" -> delay = int()
                else -> throw IllegalArgumentException("Unknown key '$key' in area gfx definition.")
            }
        }
        return CombatDefinition.AreaGfx(id, delay)
    }

}
