package content.bot.behaviour.condition

import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Being inside any of the areas listed by [ids] or tagged with one of [tags].
 * Activities with several areas are bound to a single one when assigned, see [content.bot.BotManager].
 */
data class BotInArea(val ids: List<String>, val tags: Set<String> = emptySet(), val present: Boolean = true) : Condition(1000) {

    constructor(id: String, present: Boolean = true) : this(listOf(id), emptySet(), present)

    /**
     * The first area, used when a single destination is needed
     */
    val id: String
        get() = ids.firstOrNull() ?: areas().first()

    /**
     * Whether this was written as a list of areas or tags rather than a single id
     */
    val multiple: Boolean
        get() = tags.isNotEmpty() || ids.size > 1

    /**
     * All area names this condition covers.
     */
    fun areas(): List<String> {
        if (tags.isEmpty()) {
            return ids
        }
        return (ids + tags.flatMap { tag -> Areas.tagged(tag).map { it.name } }).distinct()
    }

    /**
     * @throws IllegalArgumentException if an area doesn't exist or a tag matches no areas.
     */
    fun validate(name: String) {
        for (id in ids) {
            require(Areas.getOrNull(id) != null) { "Unknown area '$id' in $name." }
        }
        for (tag in tags) {
            require(Areas.tagged(tag).isNotEmpty()) { "No areas found with tag '$tag' in $name." }
        }
    }

    override fun keys() = if (present) areas().mapTo(mutableSetOf()) { "area:$it" } else setOf()
    override fun events() = areas().mapTo(mutableSetOf()) { "area:$it" }
    override fun check(player: Player) = areas().any { player.tile in Areas[it] } == present
}
