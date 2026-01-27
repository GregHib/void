package content.bot.action

import content.bot.req.CloneRequirement
import content.bot.req.Requirement
import content.bot.req.RequiresCarriedItem
import content.bot.req.RequiresEquippedItem
import content.bot.req.RequiresInvSpace
import content.bot.req.RequiresLocation
import content.bot.req.RequiresOwnedItem
import content.bot.req.RequiresReference
import content.bot.req.RequiresSkill
import content.bot.req.RequiresTile
import content.bot.req.RequiresVariable

data class BehaviourFragment(
    override val id: String,
    val capacity: Int,
    var template: String,
    override val requirements: List<Requirement> = emptyList(),
    override val plan: List<BotAction> = emptyList(),
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

    fun resolveRequirements(template: BotActivity, requirements: MutableList<Requirement>) {
        for (req in template.requirements) {
            val resolved = when (req) {
                is RequiresReference -> when (val requirement = req.requirement) {
                    is RequiresSkill -> RequiresSkill(
                        id = resolve(req.references["skill"], requirement.id),
                        min = resolve(req.references["min"], requirement.min),
                        max = resolve(req.references["max"], requirement.max),
                    )
                    is RequiresVariable -> RequiresVariable(
                        id = resolve(req.references["variable"], requirement.id),
                        value = resolve(req.references["value"], requirement.value),
                    )
                    is RequiresCarriedItem -> RequiresCarriedItem(
                        id = resolve(req.references["carries"], requirement.id),
                        amount = resolve(req.references["amount"], requirement.amount),
                    )
                    is RequiresEquippedItem -> RequiresEquippedItem(
                        id = resolve(req.references["equips"], requirement.id),
                        amount = resolve(req.references["amount"], requirement.amount),
                    )
                    is RequiresOwnedItem -> RequiresOwnedItem(
                        id = resolve(req.references["owns"], requirement.id),
                        amount = resolve(req.references["amount"], requirement.amount),
                    )
                    is RequiresInvSpace -> RequiresInvSpace(
                        amount = resolve(req.references["inventory_space"], requirement.amount),
                    )
                    is RequiresLocation -> RequiresLocation(
                        id = resolve(req.references["location"], requirement.id),
                    )
                    is RequiresTile -> RequiresTile(
                        x = resolve(req.references["x"], requirement.x),
                        y = resolve(req.references["y"], requirement.y),
                        level = resolve(req.references["level"], requirement.level),
                        radius = resolve(req.references["radius"], requirement.radius),
                    )
                    is CloneRequirement, is RequiresReference -> throw IllegalArgumentException("Invalid requirement type: ${req.requirement::class.simpleName}.")
                }
                is CloneRequirement -> throw IllegalArgumentException("Unresolved clone requirement in template ${id}.")
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