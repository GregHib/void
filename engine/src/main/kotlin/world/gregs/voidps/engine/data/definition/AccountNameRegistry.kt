package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.config.AccountDefinition
import java.util.concurrent.ConcurrentHashMap

/** Shared account-name metadata and an in-memory index of reserved names. */
open class AccountNameRegistry(
    protected val definitions: MutableMap<String, AccountDefinition> = ConcurrentHashMap(),
    val displayNames: MutableMap<String, String> = ConcurrentHashMap(),
) {
    @Volatile
    private var owners: Map<String, Set<String>> = emptyMap()

    init {
        indexNames()
    }

    protected fun indexNames() {
        val index = mutableMapOf<String, MutableSet<String>>()
        for (definition in definitions.values) {
            for (name in listOf(definition.accountName, definition.displayName, definition.previousName)) {
                if (name.isNotBlank()) index.getOrPut(key(name)) { mutableSetOf() }.add(key(definition.accountName))
            }
        }
        owners = index
    }

    protected fun register(definition: AccountDefinition) {
        definitions[key(definition.displayName)] = definition
        displayNames[definition.accountName.lowercase()] = definition.displayName
        indexNames()
    }

    protected fun unregister(accountName: String) {
        definitions.entries.removeIf { key(it.value.accountName) == key(accountName) }
        displayNames.remove(accountName.lowercase())
        indexNames()
    }

    fun used(name: String, exceptAccount: String? = null): Boolean =
        owners[key(name)]?.any { it != exceptAccount?.let(::key) } == true

    fun getByAccount(accountName: String): AccountDefinition? {
        return get(displayNames[accountName.lowercase()] ?: displayNames[key(accountName)] ?: return null)
    }

    fun get(displayName: String) = definitions[key(displayName)]

    fun getValue(displayName: String) = definitions.getValue(key(displayName))

    open fun update(accountName: String, newName: String, previousDisplayName: String) {
        val definition = definitions.remove(key(previousDisplayName)) ?: return
        definition.displayName = newName
        definition.previousName = previousDisplayName
        definitions[key(newName)] = definition
        displayNames[accountName.lowercase()] = newName
        indexNames()
    }

    protected fun key(name: String): String = name.lowercase().replace('_', ' ')
}
