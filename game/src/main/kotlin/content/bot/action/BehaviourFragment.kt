package content.bot.action

import content.bot.fact.*
import world.gregs.voidps.engine.event.Wildcard

data class BehaviourFragment(
    override val id: String,
    val type: String,
    val capacity: Int,
    val weight: Int,
    var template: String,
    override val requires: List<Requirement<*>> = emptyList(),
    override val setup: List<Requirement<*>> = emptyList(),
    override val actions: List<BotAction> = emptyList(),
    override val produces: Set<Requirement<*>> = emptySet(),
    val fields: Map<String, Any> = emptyMap(),
) : Behaviour {
    fun resolveActions(template: BotActivity, actions: MutableList<BotAction>) {
    }

    fun resolveRequirements(requirements: MutableList<Condition>, facts: List<Condition>) {
        for (req in facts) {
            val resolved = resolveReference(req) ?: continue
            requirements.add(resolved)
        }
    }

    private fun BehaviourFragment.resolveReference(req: Condition?): Condition? = when (req) {
        is Condition.Reference -> {
            val references = req.references
            val min = resolve(references["min"], req.min)
            val max = resolve(references["max"], req.max)
            when (req.type) {
                "skill" -> {
                    val id = resolve(references[req.type], req.id)
                    Condition.range(Fact.SkillLevel.of(id), min, max)
                }
                "carries" -> {
                    val id = resolve(references[req.type], req.id)
                    val min = resolve(references["amount"], req.min)
                    Condition.split(id, min, max, Wildcard.Item) { Fact.InventoryCount(it) }
                }
                "equips" -> {
                    val id = resolve(references[req.type], req.id)
                    val min = resolve(references["amount"], req.min)
                    Condition.split(id, min, max, Wildcard.Item) { Fact.EquipCount(it) }
                }
                "owns" -> {
                    val id = resolve(references[req.type], req.id)
                    val min = resolve(references["amount"], req.min)
                    Condition.split(id, min, max, Wildcard.Item) { Fact.ItemCount(it) }
                }
                "banked" -> {
                    val id = resolve(references[req.type], req.id)
                    val min = resolve(references["amount"], req.min)
                    Condition.split(id, min, max, Wildcard.Item) { Fact.BankCount(it) }
                }
                "clock" -> {
                    val id = resolve(references[req.type], req.id)
                    Condition.split(id, min, max, Wildcard.Variables) { Fact.ClockRemaining(it) }
                }
                "timer" -> {
                    val id = resolve(references[req.type], req.id)
                    val value = resolve(references["value"], req.value as? Boolean)
                    Condition.Equals(Fact.HasTimer(id), value as? Boolean ?: true)
                }
                "queue" -> {
                    val id = resolve(references[req.type], req.id)
                    val value = resolve(references["value"], req.value as? Boolean)
                    Condition.Equals(Fact.HasQueue(id), value as? Boolean ?: true)
                }
                "interface" -> {
                    val id = resolve(references[req.type], req.id)
                    val value = resolve(references["value"], req.value as? Boolean)
                    Condition.Equals(Fact.InterfaceOpen(id), value as? Boolean ?: true)
                }
                "variable" -> {
                    val id = resolve(references[req.type], req.id)
                    val default = resolve(references["default"], req.default)
                    if (min != null || max != null) {
                        Condition.range(Fact.IntVariable(id, default as Int), min, max)
                    } else {
                        when (val value = resolve(references["value"], req.value)) {
                            is Int -> Condition.Equals(Fact.IntVariable(id, default as Int), value)
                            is String -> Condition.Equals(Fact.StringVariable(id, default as? String), value)
                            is Double -> Condition.Equals(Fact.DoubleVariable(id, default as? Double), value)
                            is Boolean -> Condition.Equals(Fact.BoolVariable(id, default as? Boolean), value)
                            else -> null
                        }
                    }
                }
                "inventory_space" -> {
                    val min = resolve(references["inventory_space"], req.min)
                    Condition.range(Fact.InventorySpace, min, null)
                }
                "location" -> {
                    val id = resolve(references["location"], req.id)
                    Condition.Area(Fact.PlayerTile, id)
                }
                "combat_level" -> {
                    Condition.AtLeast(Fact.CombatLevel, resolve(references["combat_level"], req.min) ?: 1)
                }
                else -> null
            }
        }
        is Condition.Clone -> throw IllegalArgumentException("Unresolved clone requirement in template ${id}.")
        else -> req
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

    private fun resolve(reference: String?, default: Int?): Int? {
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