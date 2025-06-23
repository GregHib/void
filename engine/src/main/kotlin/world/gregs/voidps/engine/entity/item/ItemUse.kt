package world.gregs.voidps.engine.entity.item

/**
 * Where is this item used?
 */
enum class ItemUse {
    /**
     * Default, regular item
     */
    Surface,

    /**
     * Item used in a quest
     */
    Quest,

    /**
     * Item used in dungeoneering
     */
    Dungeoneering,

    /**
     * Item used in minigame
     */
    Minigame,

    /**
     * Item no longer in use
     */
    Removed,

    /**
     * Discontinued item
     */
    Limited,
}
