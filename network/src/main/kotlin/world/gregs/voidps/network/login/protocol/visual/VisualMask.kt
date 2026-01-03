package world.gregs.voidps.network.login.protocol.visual

object VisualMask {
    // Players
    const val PLAYER_WATCH_MASK = 0x1
    const val PLAYER_TIME_BAR_MASK = 0x400
    const val PLAYER_SAY_MASK = 0x1000
    const val PLAYER_HITS_MASK = 0x4
    const val PLAYER_FACE_MASK = 0x2
    const val PLAYER_EXACT_MOVEMENT_MASK = 0x2000
    const val PLAYER_GRAPHIC_2_MASK = 0x200
    const val PLAYER_COLOUR_OVERLAY_MASK = 0x40000
    const val MOVEMENT_TYPE_MASK = 0x80
    const val PLAYER_GRAPHIC_1_MASK = 0x20
    const val PLAYER_ANIMATION_MASK = 0x8
    const val APPEARANCE_MASK = 0x10
    const val APPEARANCE_MASK_INV = 0x10.inv()
    const val TEMPORARY_MOVEMENT_TYPE_MASK = 0x800

    // Npcs
    const val TRANSFORM_MASK = 0x2
    const val NPC_ANIMATION_MASK = 0x8
    const val NPC_GRAPHIC_1_MASK = 0x20
    const val NPC_FACE_MASK = 0x4
    const val NPC_EXACT_MOVEMENT_MASK = 0x1000
    const val NPC_COLOUR_OVERLAY_MASK = 0x2000
    const val NPC_HITS_MASK = 0x40
    const val NPC_WATCH_MASK = 0x80
    const val NPC_SAY_MASK = 0x1
    const val NPC_TIME_BAR_MASK = 0x800
    const val NPC_GRAPHIC_2_MASK = 0x400
}
