package world.gregs.voidps.network.visual

object VisualMask {
    // Players
    const val PLAYER_WATCH_MASK = 0x1
    const val PLAYER_TIME_BAR_MASK = 0x400
    const val PLAYER_FORCE_CHAT_MASK = 0x1000
    const val PLAYER_HITS_MASK = 0x4
    const val FACE_DIRECTION_MASK = 0x2
    const val PLAYER_FORCE_MOVEMENT_MASK = 0x2000
    const val PLAYER_GRAPHIC_2_MASK = 0x200
    const val PLAYER_COLOUR_OVERLAY_MASK = 0x40000
    const val TEMPORARY_MOVE_TYPE_MASK = 0x80
    const val PLAYER_GRAPHIC_1_MASK = 0x20
    const val PLAYER_ANIMATION_MASK = 0x8
    const val APPEARANCE_MASK = 0x10
    const val MOVEMENT_TYPE_MASK = 0x800

    // Npcs
    const val TRANSFORM_MASK = 0x2
    const val NPC_ANIMATION_MASK = 0x8
    const val NPC_GRAPHIC_1_MASK = 0x20
    const val TURN_MASK = 0x4
    const val NPC_FORCE_MOVEMENT_MASK = 0x1000
    const val NPC_COLOUR_OVERLAY_MASK = 0x2000
    const val NPC_HITS_MASK = 0x40
    const val NPC_WATCH_MASK = 0x80
    const val NPC_FORCE_CHAT_MASK = 0x1
    const val NPC_TIME_BAR_MASK = 0x800
    const val NPC_GRAPHIC_2_MASK = 0x400
}