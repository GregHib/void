package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.data.yaml.DefinitionConfig
import world.gregs.yaml.Yaml

/**
 * Interface representing a decoder for a set of `Definition` objects. This decoder handles
 * mappings between definitions, their IDs (both numerical and string-based), and provides
 * utility methods for accessing and transforming these elements. It also supports loading
 * definitions from external sources and applying additional metadata or operations to each definition.
 *
 * @param D The type of definition managed by the decoder, which must implement both `Definition` and `Extra`.
 */
interface DefinitionsDecoder<D> where D : Definition, D : Extra {
    /**
     * Represents an array of definitions where each element is of type `D`.
     *
     * This variable can be utilized to store and manage a collection
     * of definitions for further processing or retrieval.
     */
    var definitions: Array<D>
    /**
     * A map containing identifiers as keys and their corresponding integer values.
     * The key is a string representing a unique identifier, and the value is an integer
     * associated with that identifier.
     */
    var ids: Map<String, Int>

    /**
     * Retrieves an element from the definitions list by its identifier, or returns null
     * if the specified identifier is -1 or the element does not exist in the list.
     *
     * @param id The identifier of the element to retrieve.
     * @return The element corresponding to the given identifier, or null if the identifier is -1
     * or the element is not found.
     */
    fun getOrNull(id: Int): D? {
        if (id == -1) {
            return null
        }
        return definitions.getOrNull(id)
    }

    /**
     * Creates and returns an empty instance of type D.
     *
     * @return An empty instance of type D.
     */
    fun empty(): D

    /**
     * Retrieves the entity associated with the specified identifier.
     *
     * @param id The identifier of the entity to retrieve.
     * @return The entity of type D if found, or an empty default instance if not found.
     */
    fun get(id: Int): D {
        return getOrNull(id) ?: empty()
    }

    /**
     * Retrieves an object of type `D` corresponding to the provided identifier or returns null if not found.
     *
     * @param id The identifier as a string used to find the object.
     * @return The object of type `D` if found, or null if the identifier is invalid or the object is not available.
     */
    fun getOrNull(id: String): D? {
        if (id.isBlank()) {
            return null
        }
        val int = id.toIntOrNull()
        if (int != null) {
            return getOrNull(int)
        }
        return getOrNull(ids[id] ?: return null)
    }

    /**
     * Retrieves an object of type D associated with the given identifier.
     * If the object cannot be found, an empty object of type D is returned.
     *
     * @param id The unique identifier of the object to retrieve.
     * @return The object of type D associated with the given identifier, or an empty object if not found.
     */
    fun get(id: String): D {
        return getOrNull(id) ?: empty()
    }

    /**
     * Checks whether an element with the specified ID exists within the collection.
     *
     * @param id The identifier of the element to be checked.
     * @return `true` if an element with the specified ID exists, `false` otherwise.
     */
    fun contains(id: String): Boolean {
        return getOrNull(id) != null
    }

    /**
     * Decodes the given YAML file and processes it into an internal ID map.
     *
     * @param yaml The YAML parser instance used to load the data.
     * @param path The file path to the YAML file to be decoded.
     * @return The number of IDs processed and stored in the internal map.
     */
    fun decode(yaml: Yaml, path: String): Int {
        val ids = Object2IntOpenHashMap<String>()
        val config = DefinitionConfig(ids, definitions)
        yaml.load<Any>(path, config)
        this.ids = ids
        return ids.size
    }

    /**
     * Applies the provided names and extras to definitions and executes an optional block of code for each definition.
     *
     * @param names A map where the key is an integer representing an index, and the value is a string representing the name.
     * @param extras A nested map where the key is a string (name), and the value is another map containing additional properties.
     * @param block An optional lambda function to be executed with each individual definition as its parameter.
     */
    fun apply(names: Map<Int, String>, extras: Map<String, Map<String, Any>>, block: (D) -> Unit = {}) {
        for (i in definitions.indices) {
            val definition = definitions[i]
            val name = names[i]
            definition.stringId = name ?: i.toString()
            val extra = extras[name] ?: continue
            definition.extras = Object2ObjectOpenHashMap(extra)
            block.invoke(definition)
        }
    }

    /**
     * A companion object for utility functions related to data conversion and string manipulation.
     */
    companion object {
        /**
         * A regular expression for matching and identifying HTML/XML-like tags within a given string.
         *
         * This regex pattern specifically matches substrings that start with a less-than sign (`<`),
         * followed by any sequence of characters (including none), and ending with a greater-than sign (`>`).
         *
         * Useful for tasks such as parsing, extracting, or removing tags from strings.
         */
        private val tagRegex = "<.*?>".toRegex()

        /**
         * Removes all tags from the given text using a predefined regular expression.
         *
         * @param text The input string from which tags are to be removed.
         * @return A string with all tags stripped out.
         */
        fun removeTags(text: String) = text.replace(tagRegex, "")

        /**
         * A regular expression pattern used to identify specific punctuation characters.
         *
         * This regex matches any of the following characters: `"` (double quote), `'` (single quote),
         * `,` (comma), `(` (open parenthesis), `)` (close parenthesis), `?` (question mark), `.` (period),
         * `!` (exclamation mark).
         * Typically utilized for removing or processing these characters in text parsing or decoding operations.
         */
        private val chars = "[\"',()?.!]".toRegex()
        /**
         * Represents a predefined regular expression pattern used to match specific characters,
         * such as spaces, slashes, or hyphens, often intended for usage in text normalization or parsing.
         */
        private val underscoreChars = "[ /-]".toRegex()

        /**
         * Converts a name into a standardized identifier by performing a series of transformations,
         * including converting to lowercase, replacing specific characters, and removing tags.
         *
         * @param name The input string to be transformed into an identifier.
         * @return A standardized string identifier derived from the input name.
         */
        fun toIdentifier(name: String) =
            removeTags(name.lowercase().replace(underscoreChars, "_")).replace(chars, "").replace("&", "and").replace("à", "a").replace("é", "e").replace("ï", "i").replace("&#39;", "")
    }
}