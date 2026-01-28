package content.bot.action

import content.bot.fact.FactClone
import content.bot.fact.Fact
import content.bot.fact.CarriesItem
import content.bot.fact.EquipsItem
import content.bot.fact.HasInventorySpace
import content.bot.fact.AtLocation
import content.bot.fact.FactReference
import content.bot.fact.HasSkillLevel
import content.bot.fact.AtTile
import content.bot.fact.CarriesOne
import content.bot.fact.EquipsOne
import content.bot.fact.HasVariable
import net.pearx.kasechange.toPascalCase
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

data class BehaviourFragment(
    override val id: String,
    val type: String,
    val capacity: Int,
    val weight: Int,
    var template: String,
    override val requires: List<Fact> = emptyList(),
    override val plan: List<BotAction> = emptyList(),
    override val produces: Set<Fact> = emptySet(),
    val fields: Map<String, Any> = emptyMap(),
) : Behaviour {
    fun resolveActions(template: BotActivity, actions: MutableList<BotAction>) {
        for (action in template.plan) {
            val resolved = when (action) {
                is BotAction.Reference -> when (val copy = action.action) {
                    is BotAction.GoTo -> BotAction.GoTo(resolve(action.references["go_to"], copy.target))
                    is BotAction.InterfaceOption -> BotAction.InterfaceOption(
                        id = resolve(action.references["interface"], copy.id),
                        option = resolve(action.references["option"], copy.option),
                    )
                    is BotAction.InteractNpc -> BotAction.InteractNpc(
                        option = resolve(action.references["option"], copy.option),
                        id = resolve(action.references["npc"], copy.id),
                        retryTicks = resolve(action.references["retry_ticks"], copy.retryTicks),
                        retryMax = resolve(action.references["retry_max"], copy.retryMax),
                        radius = resolve(action.references["radius"], copy.radius),
                    )
                    is BotAction.InteractObject -> BotAction.InteractObject(
                        option = resolve(action.references["option"], copy.option),
                        id = resolve(action.references["object"], copy.id),
                        retryTicks = resolve(action.references["retry_ticks"], copy.retryTicks),
                        retryMax = resolve(action.references["retry_max"], copy.retryMax),
                        radius = resolve(action.references["radius"], copy.radius),
                    )
                    is BotAction.Wait -> BotAction.Wait(resolve(action.references["wait"], copy.ticks))
                    is BotAction.WaitFullInventory -> BotAction.WaitFullInventory(resolve(action.references["timeout"], copy.timeout))
                    is BotAction.Clone, is BotAction.Reference -> throw IllegalArgumentException("Invalid reference action type: ${action.action::class.simpleName}.")
                }
                is BotAction.Clone -> throw IllegalArgumentException("Unresolved clone action in template ${id}.")
                else -> action
            }
            actions.add(resolved)
        }
    }

    fun resolveRequirements(template: BotActivity, requirements: MutableList<Fact>) {
        for (req in template.requires) {
            val resolved = when (req) {
                is FactReference -> when (val requirement = req.fact) {
                    is HasSkillLevel -> HasSkillLevel(
                        skill = Skill.of(resolve(req.references["skill"], requirement.skill.name).toPascalCase())!!,
                        min = resolve(req.references["min"], requirement.min),
                        max = resolve(req.references["max"], requirement.max),
                    )
                    is HasVariable -> HasVariable(
                        id = resolve(req.references["variable"], requirement.id),
                        value = resolve(req.references["value"], requirement.value),
                    )
                    is CarriesItem -> CarriesItem(
                        id = resolve(req.references["carries"], requirement.id),
                        amount = resolve(req.references["amount"], requirement.amount),
                    )
                    is CarriesOne -> {
                        val resolve = resolve(req.references["equips"], "")
                        val ids = if (resolve.isBlank()) {
                            requirement.ids
                        } else {
                            Wildcards.get(resolve, Wildcard.Item)
                        }
                        CarriesOne(
                            ids = ids,
                            amount = resolve(req.references["amount"], requirement.amount),
                        )
                    }
                    is EquipsItem -> EquipsItem(
                        id = resolve(req.references["equips"], requirement.id),
                        amount = resolve(req.references["amount"], requirement.amount),
                    )
                    is EquipsOne -> {
                        val resolve = resolve(req.references["equips"], "")
                        val ids = if (resolve.isBlank()) {
                           requirement.ids
                        } else {
                            Wildcards.get(resolve, Wildcard.Item)
                        }
                        EquipsOne(
                            ids = ids,
                            amount = resolve(req.references["amount"], requirement.amount),
                        )
                    }
                    is HasInventorySpace -> HasInventorySpace(
                        amount = resolve(req.references["inventory_space"], requirement.amount),
                    )
                    is AtLocation -> AtLocation(
                        id = resolve(req.references["location"], requirement.id),
                    )
                    is AtTile -> AtTile(
                        x = resolve(req.references["x"], requirement.x),
                        y = resolve(req.references["y"], requirement.y),
                        level = resolve(req.references["level"], requirement.level),
                        radius = resolve(req.references["radius"], requirement.radius),
                    )
                    is FactClone, is FactReference -> throw IllegalArgumentException("Invalid requirement type: ${req.fact::class.simpleName}.")
                }
                is FactClone -> throw IllegalArgumentException("Unresolved clone requirement in template ${id}.")
                else -> req
            }
            requirements.add(resolved)
        }
    }

    private fun String.key(): String {
        if (startsWith('$')) {
            return substring(1)
        }
        val index = indexOf($$"${")
        if (index == -1) {
            return substringAfter('$')
        }
        val end = indexOf('}', index + 2)
        return substring(index + 2, end)
    }

    private fun String.reference(): String {
        if (startsWith('$')) {
            return this
        }
        val index = indexOf($$"${")
        if (index == -1) {
            return "\$${substringAfter('$')}"
        }
        val end = indexOf('}', index) + 1
        return substring(index, end)
    }

    private fun resolve(reference: String?, default: Int): Int {
        return if (reference != null) {
            fields[reference.key()] as? Int ?: throw IllegalArgumentException("Unable to find field '$reference' in ${id}.")
        } else {
            default
        }
    }

    private fun resolve(reference: String?, default: String): String {
        return if (reference != null) {
            val key = reference.key()
            val value = fields[key] as? String ?: throw IllegalArgumentException("Unable to find field '$reference' in ${id}.")
            reference.replace(reference.reference(), value)
        } else {
            default
        }
    }

    private fun resolve(reference: String?, default: Any?): Any? {
        return if (reference != null) {
            fields[reference.key()] ?: throw IllegalArgumentException("Unable to find field '$reference' in ${id}.")
        } else {
            default
        }
    }
}