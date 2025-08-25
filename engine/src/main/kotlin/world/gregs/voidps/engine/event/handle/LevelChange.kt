package world.gregs.voidps.engine.event.handle

import com.squareup.kotlinpoet.ClassName
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.EventProcessor

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class LevelChange(
    vararg val skills: Skill,
    val ids: Array<String> = [],
    val from: Int = -1,
    val to: Int = -1,
)

object LevelChangeSchema : EventProcessor.SchemaProvider {
    private val entities = setOf(
        Player::class.simpleName, NPC::class.simpleName
    )

    override fun param(param: ClassName): String {
        if (entities.contains(param.simpleName)) {
            return "it"
        }
        return super.param(param)
    }

    override fun schema(extension: String, params: List<ClassName>, data: Map<String, Any?>) = when (extension) {
        "MaxLevelChanged" -> listOf(
            params.key("_max_level_change"),
            EventField {
                (it["skills"] as List<Skill>).toSet() // FIXME prob KSType?
            },
            params.identifier(),
            EventField.IntKey("from"),
            EventField.IntKey("to"),
        )
        else -> emptyList()
    }
}