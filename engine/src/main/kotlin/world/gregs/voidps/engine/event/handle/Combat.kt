package world.gregs.voidps.engine.event.handle

import com.squareup.kotlinpoet.ClassName
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.EventProcessor

/**
 * Timer base events
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Combat(
    vararg val ids: String,
)

object CombatSchema : EventProcessor.SchemaProvider {
    private val entities = setOf(
        Player::class.simpleName, NPC::class.simpleName, Character::class.simpleName, FloorItem::class.simpleName, GameObject::class.simpleName, World::class.simpleName,
    )

    override fun param(param: ClassName): String {
        if (entities.contains(param.simpleName)) {
            return "it"
        }
        return super.param(param)
    }

    override fun schema(extension: String, params: List<ClassName>, data: Map<String, Any?>) = when (extension) {
        "CombatAttack" -> listOf(
            params.key("combat_attack"),
            params.identifier(),
            EventField.StringKey("weapon"),
            EventField.StringKey("style"),
            EventField.StringKey("spell"),
        )
        "CombatDamage" -> listOf(
            params.key("combat_damage"),
            params.identifier(),
            EventField.StringKey("weapon"),
            EventField.StringKey("style"),
            EventField.StringKey("spell"),
        )
        "CombatPrepare" -> listOf(
            params.key("combat_prepare"),
            params.identifier(),
            EventField.StringKey("style"),
        )
        "CombatSwing" -> listOf(
            params.key("combat_swing"),
            params.identifier(),
            EventField.StringKey("weapon"),
            EventField.StringKey("style"),
        )
        else -> emptyList()
    }
}
