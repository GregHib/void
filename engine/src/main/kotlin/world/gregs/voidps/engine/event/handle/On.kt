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
 * Misc events
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class On(
    vararg val ids: String,
)

object OnSchema : EventProcessor.SchemaProvider {
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
        "Consume" -> listOf(
            EventField.StaticValue("consume"),
            EventField.StringList("ids"),
        )
        "Spawn" -> listOf(
            params.key("spawn"),
            params.identifier()
        )
        "Despawn" -> listOf(
            params.key("despawn"),
            params.identifier()
        )
        "Destroyed" -> listOf(
            EventField.StaticValue("destroy"),
            EventField.StringList("ids")
        )
        "InterfaceOpened" -> listOf(
            EventField.StaticValue("interface_open"),
            EventField.StringList("ids")
        )
        "InterfaceClosed" -> listOf(
            EventField.StaticValue("interface_close"),
            EventField.StringList("ids")
        )
        "InterfaceRefreshed" -> listOf(
            EventField.StaticValue("interface_refresh"),
            EventField.StringList("ids")
        )
        "CombatStart" -> listOf(
            params.key("combat_start"),
            params.identifier(),
        )
        "CombatStop" -> listOf(
            params.key("combat_stop"),
            params.identifier(),
        )
        "SpecialAttack" -> listOf(
            EventField.StaticValue("special_attack"),
            EventField.StringList("ids")
        )
        "SpecialAttackDamage" -> listOf(
            EventField.StaticValue("special_attack_damage"),
            EventField.StringList("ids")
        )
        "SpecialAttackPrepare" -> listOf(
            EventField.StaticValue("special_attack_prepare"),
            EventField.StringList("ids")
        )
        else -> emptyList()
    }

}
