package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.BodyParts
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.item.BodyPart
import world.gregs.voidps.engine.entity.item.EquipSlot

data class Appearance(
    var male: Boolean = true,
    var showSkillLevel: Boolean = false,
    var skillLevel: Int = -1,
    var size: Int = 1,
    var trimTitle: Boolean = false,
    var title: Int = -1,
    var prefix: String = "",
    var skull: Int = -1,
    var headIcon: Int = -1,
    var hidden: Boolean = false,
    var transform: Int = -1,
    val body: BodyParts,
    val colours: IntArray = IntArray(5),
    var emote: Int = 1426,
    var displayName: String = "",
    var combatLevel: Int = 3,
    var summoningCombatLevel: Int = 0,
    var idleSound: Int = -1,
    var crawlSound: Int = -1,
    var walkSound: Int = -1,
    var runSound: Int = -1,
    var soundDistance: Int = 0
) : Visual {
}

const val APPEARANCE_MASK = 0x10

fun Player.flagAppearance() = visuals.flag(APPEARANCE_MASK)

val Player.appearance: Appearance
    get() = visuals.getOrPut(APPEARANCE_MASK) {
        val bodyParts = BodyParts(equipment, intArrayOf(3, 14, 18, 26, 34, 38, 42))
        BodyPart.all.forEach {
            bodyParts.updateConnected(it)
        }
        updateAppearanceOnEquipmentChanges(bodyParts)
        Appearance(body = bodyParts)
    }

private fun Player.updateAppearanceOnEquipmentChanges(parts: BodyParts) {
    events.on<Player, ItemChanged>({ container == "worn_equipment" && needsUpdate(index, parts) }) {
        flagAppearance()
    }
}

private fun needsUpdate(index: Int, parts: BodyParts): Boolean {
    val slot = EquipSlot.by(index)
    val part = BodyPart.by(slot) ?: return false
    return parts.updateConnected(part)
}

private fun Player.flag(action: Appearance.() -> Unit) {
    action(appearance)
    flagAppearance()
}

var Player.male: Boolean
    get() = appearance.male
    set(value) = flag {
        male = value
    }

fun Player.toggleSkillLevel() = flag {
    showSkillLevel = !showSkillLevel
}

fun Player.setTrimTitle(trim: Boolean = false) = flag {
    this.trimTitle = trim
}

var Player.title: Int
    get() = appearance.title
    set(value) = flag {
        title = value
    }

var Player.prefix: String
    get() = appearance.prefix
    set(value) = flag {
        prefix = value
    }

var Player.headIcon: Int
    get() = appearance.headIcon
    set(value) = flag {
        headIcon = value
    }

fun Player.setLook(index: Int, look: Int) = flag {
    val part = BodyPart.by(index) ?: return@flag
    body.looks[index] = look
    body.updateConnected(part)
}

val Player.looks: IntArray
    get() = appearance.body.looks

fun Player.setColour(index: Int, colour: Int) = flag {
    this.colours[index] = colour
}

val Player.colours: IntArray
    get() = appearance.colours

var Player.emote: Int
    get() = appearance.emote
    set(value) = flag {
        appearance.emote = value
    }

var Player.name: String
    get() = appearance.displayName
    set(value) = flag {
        displayName = value
    }

var Player.combatLevel: Int
    get() = appearance.combatLevel
    set(value) = flag {
        combatLevel = value
    }

var Player.skillLevel: Int
    get() = appearance.skillLevel
    set(value) = flag {
        skillLevel = value
    }

var Player.summoningCombatLevel: Int
    get() = appearance.summoningCombatLevel
    set(value) = flag {
        summoningCombatLevel = value
    }