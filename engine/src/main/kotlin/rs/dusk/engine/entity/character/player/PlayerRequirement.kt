package rs.dusk.engine.entity.character.player

interface PlayerRequirement {
    fun onFailure(player: Player)
    fun met(player: Player): Boolean
}

fun Player.lacks(requirement: PlayerRequirement): Boolean {
    if (!requirement.met(this)) {
        requirement.onFailure(this)
        return true
    }
    return false
}