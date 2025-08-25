package world.gregs.voidps.engine.event.handle

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.EventProcessor
import world.gregs.voidps.type.Tile

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Move(
    vararg val ids: String,
    val from: Int = -1,
    val to: Int = -1,
)

object MoveSchema : EventProcessor.SchemaProvider {
    private val entities = setOf(
        Player::class.simpleName, NPC::class.simpleName
    )

    override fun param(param: ClassName): String {
        if (entities.contains(param.simpleName)) {
            return "it"
        }
        return super.param(param)
    }

    override fun schema(extension: String, params: List<ClassName>, data: Map<String, Any?>): List<EventField> = when (extension) {
        "Moved<Player>" -> listOf(
            EventField.StaticValue("player_move"),
            EventField.StaticValue("player"),
            EventField.IntKey("from"),
            EventField.IntKey("to"),
        )
        "Moved<NPC>" -> listOf(
            EventField.StaticValue("player_move"),
            EventField.StringList("ids"),
            EventField.IntKey("from"),
            EventField.IntKey("to"),
        )
        else -> emptyList()
    }

}