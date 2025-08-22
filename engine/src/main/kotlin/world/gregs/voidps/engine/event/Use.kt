package world.gregs.voidps.engine.event

/**
 * TargetInteraction's
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Use(
    val option: String = "*",
    val ids: Array<String> = [],
    val npcs: Array<String> = [],
    val items: Array<String> = [],
    val id: String = "",
    val component: String = "*",
    val approach: Boolean = false,
    val arrive: Boolean = true,
)

/**
 * Interface/Item on Entities
 * @param use [interface, component] or items
 * @param on id of the entity
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class UseOn(
    val use: Array<String> = [],
    val on: Array<String> = [],
    val approach: Boolean = false,
    val arrive: Boolean = true,
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Option(
    val option: String = "*",
    vararg val ids: String,
    val approach: Boolean = false,
    val arrive: Boolean = true,
)

/*
    InterfaceOnNPC
    id
    component
    npc
    approach

    InterfaceOnObject
    id
    component
    obj
    arrive
    approach

    InterfaceOnPlayer
    id
    component
    approach

    ItemOnNPC
    item
    npc
    approach

    ItemOnObject
    item
    obj
    arrive
    approach

    ItemOnPlayer
    item
    approach

    NPCOption<Player, NPC, Character>
    option
    npc(s)
    approach

    PlayerOption<Player, NPC, Character>
    option
    npc(s)
    approach

    FloorItemOption<Player, NPC, Character>
    option
    item
    npc
    arrive
    approach

    ObjectOption<Player, NPC, Character>
    option
    objects
    npc
    arrive
    approach


 */