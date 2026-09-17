package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.RenderEmoteDefinitions
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.encode.Friend
import world.gregs.voidps.network.login.protocol.encode.sendFriendsList
import world.gregs.voidps.network.login.protocol.visual.VisualMask
import world.gregs.voidps.network.login.protocol.visual.update.player.Appearance
import world.gregs.voidps.network.login.protocol.visual.update.player.MoveType

fun Player.flagMovementType() = visuals.flag(VisualMask.MOVEMENT_TYPE_MASK)

fun Player.flagAppearance() {
    visuals.flag(VisualMask.APPEARANCE_MASK)
    appearance.hash = appearance.hashCode()
    appearance.length = appearance.length()
}

fun Player.flagTemporaryMoveType() = visuals.flag(VisualMask.TEMPORARY_MOVEMENT_TYPE_MASK)

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
        val definition = NPCDefinitions.get(id)
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

/**
 * Whether [name] is already in use as another account's display name
 */
fun Player.nameTaken(name: String): Boolean {
    val existing = get<AccountDefinitions>().get(name) ?: return false
    return existing.accountName != accountName
}

/**
 * Changes the display name and notifies online friends of the new name
 */
fun Player.rename(toName: String) {
    val previous = name
    name = toName
    val friend = Friend(toName, previous, renamed = true, world = Settings.world, worldName = Settings.worldName)
    for (other in Players) {
        if (other == this || !other.friends.contains(accountName)) {
            continue
        }
        other.client?.sendFriendsList(listOf(friend))
    }
}

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

var Player.temporaryMoveType: MoveType
    get() = visuals.temporaryMoveType.type
    set(value) {
        if (visuals.temporaryMoveType.type != value) {
            visuals.temporaryMoveType.type = value
            flagTemporaryMoveType()
        }
    }

var Player.movementType: MoveType
    get() = visuals.movementType.type
    set(value) {
        if (visuals.movementType.type != value) {
            visuals.movementType.type = value
            flagMovementType()
        }
    }
