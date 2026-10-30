import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.io.File

/**
 * Gradle task which incrementally collects script classes inside a given directory.
 *
 * Every class declaration (and its supertypes) is kept in [indexFile] so classes which implement Script indirectly,
 * via a base class or interface declared in another file, are still found when only some files have changed.
 */
abstract class ScriptMetadataTask : DefaultTask() {

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: DirectoryProperty

    @get:Internal
    abstract var resourceDirectory: File

    @get:OutputFile
    abstract var scriptsFile: File

    @get:OutputFile
    abstract val indexFile: RegularFileProperty

    init {
        description = "Analyzes Kotlin files and extracts list of classes which extend Script interface"
        group = "metadata"
        indexFile.convention(project.layout.buildDirectory.file("script-metadata/index.txt"))
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val start = System.currentTimeMillis()
        val root = inputDirectory.get().asFile
        val index = indexFile.get().asFile
        val files: MutableMap<String, List<Declaration>>
        if (!inputChanges.isIncremental) {
            logger.info("Non-incremental run: analyzing all files")
            files = mutableMapOf()
            for (change in inputChanges.getFileChanges(inputDirectory)) {
                if (change.changeType != ChangeType.REMOVED) {
                    update(files, root, change.file)
                }
            }
        } else if (!index.exists()) {
            logger.info("No script index found: analyzing all files")
            files = mutableMapOf()
            root.walkTopDown().forEach { update(files, root, it) }
        } else {
            files = readIndex(index)
            for (change in inputChanges.getFileChanges(inputDirectory)) {
                if (change.changeType == ChangeType.REMOVED) {
                    files.remove(change.file.relativeTo(root).invariantSeparatorsPath)
                } else {
                    update(files, root, change.file)
                }
            }
        }
        writeIndex(index, files)
        val scripts = resolveScripts(files)
        scriptsFile.writeText(scripts.joinToString("\n"))
        println("Metadata for ${scripts.size} scripts collected in ${System.currentTimeMillis() - start} ms")
    }

    private fun update(files: MutableMap<String, List<Declaration>>, root: File, file: File) {
        if (!file.isFile || !file.name.endsWith(".kt")) {
            return
        }
        files[file.relativeTo(root).invariantSeparatorsPath] = parseDeclarations(file.readText())
    }

    private fun readIndex(file: File): MutableMap<String, List<Declaration>> {
        val files = mutableMapOf<String, MutableList<Declaration>>()
        for (line in file.readLines()) {
            if (line.isBlank()) {
                continue
            }
            val parts = line.split("|")
            val supertypes = if (parts[3].isEmpty()) emptyList() else parts[3].split(",")
            files.getOrPut(parts[0]) { mutableListOf() }.add(Declaration(parts[1], parts[2].toBoolean(), supertypes))
        }
        return files.toMutableMap()
    }

    private fun writeIndex(file: File, files: Map<String, List<Declaration>>) {
        file.parentFile.mkdirs()
        file.writeText(
            files.entries.sortedBy { it.key }.joinToString("\n") { (path, declarations) ->
                declarations.joinToString("\n") { "$path|${it.name}|${it.instantiable}|${it.supertypes.joinToString(",")}" }
            },
        )
    }

    /**
     * Instantiable classes which implement Script either directly or through any chain of other declared types.
     */
    private fun resolveScripts(files: Map<String, List<Declaration>>): List<String> {
        val bySimpleName = files.values.flatten().groupBy { it.name.substringAfterLast('.').substringAfterLast('$') }
        val known = mutableMapOf<String, Boolean>()
        fun isScript(declaration: Declaration, visiting: MutableSet<String>): Boolean = known.getOrPut(declaration.name) {
            if (!visiting.add(declaration.name)) {
                return false
            }
            declaration.supertypes.any { type -> type == "Script" || bySimpleName[type]?.any { isScript(it, visiting) } == true }
        }
        return files.entries.sortedBy { it.key }
            .flatMap { it.value }
            .filter { it.instantiable && isScript(it, mutableSetOf()) }
            .map { it.name }
    }

    /**
     * @param name binary class name e.g. `content.skill.Fishing` or `content.skill.Outer$Inner`
     * @param instantiable whether the declaration is a concrete class which can be constructed
     * @param supertypes simple names of the direct supertypes
     */
    data class Declaration(val name: String, val instantiable: Boolean, val supertypes: List<String>)

    companion object {
        private val PACKAGE_REGEX = Regex("""^\s*package\s+([\w.]+)""", RegexOption.MULTILINE)
        private val DECLARATION_REGEX = Regex("""(?<![\w.:])((?:(?:public|private|internal|protected|abstract|open|final|data|sealed|inner|enum|annotation|value|inline|fun|companion|expect|actual|external)\s+)*)(class|interface|object)\s+(\w+)""")
        private val CONSTRUCTOR_PREFIX_REGEX = Regex("""\G\s*(?:(?:@\w+(?:\.\w+)*\s*(?:\([^()]*\))?|public|private|internal|protected)\s+)*constructor\b""")

        fun parseDeclarations(source: String): List<Declaration> {
            val text = stripCommentsAndStrings(source)
            val packageName = PACKAGE_REGEX.find(text)?.groupValues?.get(1)
            val headers = DECLARATION_REGEX.findAll(text).map { parseHeader(text, it) }.toList()
            // Walk braces to work out which declarations are nested inside others
            val bodies = headers.filter { it.body != -1 }.associateBy { it.body }
            val stack = ArrayDeque<Pair<String, Int>>()
            val names = mutableMapOf<Header, String>()
            var depth = 0
            var next = 0
            for (i in text.indices) {
                while (next < headers.size && headers[next].start == i) {
                    val header = headers[next++]
                    val outer = stack.joinToString("") { "${it.first}$" }
                    names[header] = if (packageName == null) "$outer${header.name}" else "$packageName.$outer${header.name}"
                }
                when (text[i]) {
                    '{' -> {
                        depth++
                        val header = bodies[i]
                        if (header != null) {
                            stack.addLast(header.name to depth)
                        }
                    }
                    '}' -> {
                        if (stack.lastOrNull()?.second == depth) {
                            stack.removeLast()
                        }
                        depth--
                    }
                }
            }
            return headers.map { header -> Declaration(names.getValue(header), header.instantiable, header.supertypes) }
        }

        private class Header(val start: Int, val name: String, val instantiable: Boolean, val supertypes: List<String>, val body: Int)

        private fun parseHeader(text: String, match: MatchResult): Header {
            val modifiers = match.groupValues[1].split(Regex("\\s+")).toSet()
            val kind = match.groupValues[2]
            val instantiable = kind == "class" && modifiers.none { it == "abstract" || it == "sealed" || it == "enum" || it == "annotation" || it == "inner" }
            var i = skipWhitespace(text, match.range.last + 1)
            if (i < text.length && text[i] == '<') {
                i = skipWhitespace(text, skipBalanced(text, i, '<', '>'))
            }
            val constructor = CONSTRUCTOR_PREFIX_REGEX.find(text, i)
            if (constructor != null && constructor.range.first == i) {
                i = skipWhitespace(text, constructor.range.last + 1)
            }
            if (i < text.length && text[i] == '(') {
                i = skipWhitespace(text, skipBalanced(text, i, '(', ')'))
            }
            val supertypes = mutableListOf<String>()
            if (i < text.length && text[i] == ':') {
                i = parseSupertypes(text, i + 1, supertypes)
            }
            i = skipWhitespace(text, i)
            val body = if (i < text.length && text[i] == '{') i else -1
            return Header(match.range.first, match.groupValues[3], instantiable, supertypes, body)
        }

        /**
         * Reads a comma separated list of supertypes until the class body, a `where` clause or the end of the declaration.
         * @return index after the last supertype
         */
        private fun parseSupertypes(text: String, start: Int, supertypes: MutableList<String>): Int {
            var i = start
            val entry = StringBuilder()
            while (i < text.length) {
                val char = text[i]
                when {
                    char == '(' -> i = skipBalanced(text, i, '(', ')').also { entry.append(' ') } - 1
                    char == '<' -> i = skipBalanced(text, i, '<', '>').also { entry.append(' ') } - 1
                    char == ',' -> {
                        supertypes.add(typeName(entry))
                        entry.clear()
                    }
                    char == '{' || char == '}' || char == ';' || char == '=' -> break
                    char == '\n' && entry.isNotBlank() -> {
                        // A newline ends the declaration unless the list continues with a comma
                        val nextChar = skipWhitespace(text, i)
                        if (nextChar >= text.length || text[nextChar] != ',') {
                            break
                        }
                    }
                    else -> entry.append(char)
                }
                i++
            }
            if (entry.isNotBlank()) {
                supertypes.add(typeName(entry))
            }
            return i
        }

        private fun typeName(entry: CharSequence): String {
            val tokens = entry.trim().split(Regex("\\s+")).filter { !it.startsWith("@") }
            val type = tokens.firstOrNull { it != "where" } ?: return ""
            return type.substringAfterLast('.').removeSuffix("?")
        }

        private fun skipWhitespace(text: String, start: Int): Int {
            var i = start
            while (i < text.length && text[i].isWhitespace()) {
                i++
            }
            return i
        }

        /**
         * @return index after the [close] matching the [open] at [start]
         */
        private fun skipBalanced(text: String, start: Int, open: Char, close: Char): Int {
            var depth = 0
            var i = start
            while (i < text.length) {
                val char = text[i]
                if (char == open) {
                    depth++
                } else if (char == close && !(close == '>' && i > 0 && text[i - 1] == '-')) {
                    depth--
                    if (depth == 0) {
                        return i + 1
                    }
                }
                i++
            }
            return i
        }

        /**
         * Replaces comments and the contents of string and char literals with spaces so brackets inside them
         * can't confuse the parser. String template expressions are kept as code.
         */
        fun stripCommentsAndStrings(source: String): String {
            val out = StringBuilder(source.length)
            // Nested literal state: STRING, RAW_STRING, or the open brace count (>= 0) of a `${}` template expression
            val stack = ArrayDeque<Int>()
            var i = 0
            while (i < source.length) {
                val char = source[i]
                val top = stack.lastOrNull()
                if (top == STRING || top == RAW_STRING) {
                    when {
                        top == STRING && char == '\\' && i + 1 < source.length -> {
                            out.append("  ")
                            i += 2
                        }
                        source.startsWith("\${", i) -> {
                            out.append("  ")
                            stack.addLast(0)
                            i += 2
                        }
                        top == RAW_STRING && source.startsWith("\"\"\"", i) -> {
                            out.append("\"\"\"")
                            stack.removeLast()
                            i += 3
                        }
                        top == STRING && char == '"' -> {
                            out.append('"')
                            stack.removeLast()
                            i++
                        }
                        else -> {
                            out.append(if (char == '\n') '\n' else ' ')
                            i++
                        }
                    }
                    continue
                }
                when {
                    source.startsWith("//", i) -> {
                        while (i < source.length && source[i] != '\n') {
                            out.append(' ')
                            i++
                        }
                    }
                    source.startsWith("/*", i) -> {
                        var depth = 0
                        while (i < source.length) {
                            if (source.startsWith("/*", i)) {
                                depth++
                                out.append("  ")
                                i += 2
                            } else if (source.startsWith("*/", i)) {
                                depth--
                                out.append("  ")
                                i += 2
                                if (depth == 0) {
                                    break
                                }
                            } else {
                                out.append(if (source[i] == '\n') '\n' else ' ')
                                i++
                            }
                        }
                    }
                    source.startsWith("\"\"\"", i) -> {
                        out.append("\"\"\"")
                        stack.addLast(RAW_STRING)
                        i += 3
                    }
                    char == '"' -> {
                        out.append('"')
                        stack.addLast(STRING)
                        i++
                    }
                    char == '\'' -> {
                        // Char literal: 'a', '\n', '\''
                        var end = i + 1
                        if (end < source.length && source[end] == '\\') {
                            end++
                        }
                        end = source.indexOf('\'', end + 1).takeIf { it != -1 } ?: (source.length - 1)
                        out.append('\'')
                        repeat(end - i - 1) { out.append(' ') }
                        out.append('\'')
                        i = end + 1
                    }
                    char == '{' && top != null -> {
                        stack.addLast(stack.removeLast() + 1)
                        out.append(char)
                        i++
                    }
                    char == '}' && top != null -> {
                        if (top == 0) {
                            // End of template expression, back to the enclosing string
                            stack.removeLast()
                            out.append(' ')
                        } else {
                            stack.addLast(stack.removeLast() - 1)
                            out.append(char)
                        }
                        i++
                    }
                    else -> {
                        out.append(char)
                        i++
                    }
                }
            }
            return out.toString()
        }

        private const val STRING = -1
        private const val RAW_STRING = -2
    }
}
