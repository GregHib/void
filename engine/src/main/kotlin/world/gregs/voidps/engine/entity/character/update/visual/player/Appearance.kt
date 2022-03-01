package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrPut
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.visual.BodyPart
import world.gregs.voidps.network.visual.VisualMask.APPEARANCE_MASK
import world.gregs.voidps.network.visual.update.player.Appearance

fun Player.flagAppearance() = visuals.flag(APPEARANCE_MASK)

val Player.appearance: Appearance
    get() = visuals.appearance

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
    get() = this["display_name", accountName]
    set(value) = flag {
        val previous = name
        displayName = value
        set("display_name", true, value)
        nameHistory.add(previous)
        get<AccountDefinitions>().update(accountName, value, previous)
    }

val Player.nameHistory: MutableList<String>
    get() = getOrPut("name_history", true) { mutableListOf() }

val Player.previousName: String
    get() = nameHistory.firstOrNull() ?: ""

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