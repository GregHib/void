package world.gregs.voidps.engine.path.algorithm

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRequirement
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject

object ActionsTest {

    fun require() {

    }

    fun Unit.skill(skill: Skill, level: Int) {

    }

    fun Unit.quest(name: String) {

    }

    fun Unit.meets(requirement: PlayerRequirement) {

    }

    fun Unit.can(predicate: () -> Boolean) {

    }

    fun use(vararg values: String) {

    }

    fun Unit.on(vararg values: String, action: () -> Unit) {

    }

    fun <T : Entity> Unit.on(vararg values: String, action: (T) -> Unit) {

    }

    fun on(vararg values: String, action: () -> Unit) {

    }

    fun on(vararg values: String, action: (Container, Int, GameObject) -> Unit) {

    }

    fun on(vararg values: String, action: (Container, Int) -> Unit) {

    }

    inline fun <reified T : Entity> on(vararg values: String, action: (T) -> Unit) {
        if (T::class == GameObject::class) {

        }
    }

    class Option<T : Entity>(val requirements: Set<PlayerRequirement>, val action: (Player, T) -> Unit)

    class UseOption<T : Entity>(val requirements: Set<PlayerRequirement>, val action: (Player, Container, Int, T) -> Unit)

    class ActionBuilder {

    }

    val action = ActionBuilder()

    init {
        val flow = MutableSharedFlow<Any>()

        runBlocking {
            flow.filter { true }.collect { }
        }
        action
        require()
            .skill(Skill.Woodcutting, 12)
            .quest("tree_choppers_paradise")
            .can { "not-wc-blocked"; true }
            .on("cut-down maple_tree") { tree: GameObject ->

            }


//        on("talk_to", "bob") { bob: NPC ->
//
//        }.meets(skill(Skill.Woodcutting, 12))
//            .meets(skill(Skill.Woodcutting, 12))
        on("use", "inventory", "item", "on", "bank_booth") { inv, index, booth ->

        }
        on("use inventory bronze_axe on bank_booth") { inv, index, booth ->

        }
        on("talk_to general_store_owner") { owner: NPC ->

        }
        on("equipment equip abyssal_whip") { equipment: Container, index: Int ->
        }

        use("inventory", "item")
            .on("bank_booth") {

            }
    }
}