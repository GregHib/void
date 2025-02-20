package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.RenderEmoteDefinitions
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.visual.VisualMask
import world.gregs.voidps.network.login.protocol.visual.update.player.Appearance
import world.gregs.voidps.network.login.protocol.visual.update.player.MoveType

fun Player.flagTemporaryMoveType() = visuals.flag(VisualMask.TEMPORARY_MOVE_TYPE_MASK)

fun Player.flagAppearance() {
    visuals.flag(VisualMask.APPEARANCE_MASK)
    appearance.hash = appearance.hashCode()
    appearance.length = appearance.length()
}

fun Player.flagMovementType() = visuals.flag(VisualMask.MOVEMENT_TYPE_MASK)

val Player.appearance: Appearance
    get() = visuals.appearance

private fun Player.flag(action: Appearance.() -> Unit) {
    action(appearance)
    flagAppearance()
}

var Player.male: Boolean
    get() = body.male
    set(value) = flag {
        this@male.body.male = value
    }

val Player.sex: String
    get() = if (male) "male" else "female"

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

fun Player.renderEmote(id: String) = flag {
    val definition = get<RenderEmoteDefinitions>().get(id)
    appearance.emote = definition.id
}

fun Player.clearRenderEmote() = flag {
    val id: String? = this@clearRenderEmote["transform_id"]
    if (id == null) {
        emote = 1426
    } else {
        val definition = get<NPCDefinitions>().get(id)
        emote = definition.renderEmote
    }
}

var Player.name: String
    get() = this["display_name", accountName]
    set(value) = flag {
        val previous = name
        displayName = value
        set("display_name", value)
        nameHistory.add(previous)
        get<AccountDefinitions>().update(accountName, value, previous)
    }

val Player.nameHistory: MutableList<String>
    get() = getOrPut("name_history") { mutableListOf() }

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

var Player.movementType: MoveType
    get() = visuals.movementType.type
    set(value) {
        if (visuals.movementType.type != value) {
            visuals.movementType.type = value
            flagMovementType()
        }
    }

var Player.temporaryMoveType: MoveType
    get() = visuals.temporaryMoveType.type
    set(value) {
        if (visuals.temporaryMoveType.type != value) {
            visuals.temporaryMoveType.type = value
            flagTemporaryMoveType()
        }
    }