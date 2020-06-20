package rs.dusk.engine.event

object Priority {
    val LOGIN_QUEUE = 100
    val VIEWPORT = 99
    val PLAYER_MOVEMENT = 98
    val NPC_MOVEMENT = 97
    val PLAYER_VISUALS = 96
    val NPC_VISUALS = 95
    val PLAYER_CHANGE = 94
    val NPC_CHANGE = 93
    val PLAYER_UPDATE = 92
    val NPC_UPDATE = 91
    val PLAYER_UPDATE_FINISHED = 90
    val NPC_UPDATE_FINISHED = 89

    val HIGHEST = 10
    val HIGH = 5
    val NORMAL = 0
    val LOW = -5
    val LOWEST = -10
}