package rs.dusk.engine.entity.character

class CharacterEffects(private val character: Character) {
    private val effects = mutableMapOf<String, Effect>()

    fun toggle(effect: Effect): Boolean {
        if (!remove(effect)) {
            return add(effect)
        }
        return true
    }

    fun add(effect: Effect): Boolean {
        if (!effect.immune(character)) {
            val current = getOrNull(effect.effectType)
            if (current == null || remove(current)) {
                effect.onStart(character)
                effects[effect.effectType] = effect
            }
        }
        return true
    }

    fun remove(effect: Effect): Boolean {
        if (effects.remove(effect.effectType, effect)) {
            finish(effect)
            return true
        }
        return false
    }

    fun has(type: String): Boolean {
        return effects.containsKey(type)
    }

    fun getOrNull(type: String): Effect? {
        return effects[type]
    }

    fun remove(type: String): Boolean {
        val effect = getOrNull(type) ?: return false
        return remove(effect)
    }

    fun removeAll() {
        val iterator = effects.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            finish(next.value)
            iterator.remove()
        }
    }

    private fun finish(effect: Effect) {
        effect.onFinish(character)
    }

    fun getAll(): Map<String, Effect> {
        return effects
    }
}
