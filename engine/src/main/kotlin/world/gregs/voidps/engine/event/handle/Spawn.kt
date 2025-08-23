package world.gregs.voidps.engine.event.handle

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.EventProcessor

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Spawn(
    vararg val ids: String
)

object SpawnSchema : EventProcessor.SchemaProvider {
    private val set = setOf(
        Player::class.simpleName, NPC::class.simpleName, Character::class.simpleName, FloorItem::class.simpleName, GameObject::class.simpleName, World::class.simpleName,
    )

    override fun param(param: ClassName): String {
        if (set.contains(param.simpleName)) {
            return "it"
        }
        return super.param(param)
    }

    override fun extension() = Spawn::class.asClassName()

    override fun schema(extension: String, params: List<ClassName>) = listOf(
        EventField.StaticSet(type(params)),
        EventField.StringList("ids")
    )

    fun type(params: List<ClassName>): Set<String> {
        for (param in params) {
            when (param.simpleName) {
                Player::class.simpleName -> return setOf("player_spawn")
                NPC::class.simpleName -> return setOf("npc_spawn")
                Character::class.simpleName -> return setOf("player_spawn", "npc_spawn")
                FloorItem::class.simpleName -> return setOf("floor_item_spawn")
                GameObject::class.simpleName -> return setOf("object_spawn")
                World::class.simpleName -> return setOf("world_spawn")
            }
        }
        throw IllegalArgumentException("Expected Spawn method to have a entity parameter e.g. \"@Spawn fun worldSpawn(world: World) {}\"")
    }
}