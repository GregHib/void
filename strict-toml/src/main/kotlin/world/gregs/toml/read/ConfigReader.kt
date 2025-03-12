package world.gregs.toml.read

import java.io.BufferedInputStream
import java.io.File

object ConfigReader {
    interface Api {
        fun set(section: String, key: String, value: Any)
        fun set(section: String, key: String, value: String)
        fun set(section: String, key: String, value: Long)
        fun set(section: String, key: String, value: Double)
        fun set(section: String, key: String, value: Boolean)
        fun set(section: String, key: String, values: List<Any>)
        fun set(section: String, key: String, values: Map<String, Any>)
    }

    class IniParser(private val api: Api) {
        private val buffer = ByteArray(1024)
        private var bufferIndex = 0
        private var section = ""

        fun parse(file: File) {
            val input = file.inputStream().buffered()
            var byte = input.read()

            while (byte != -1) {
                when (byte) {
                    ' '.code, '\t'.code -> {
                        // Skip whitespace
                        while (byte == ' '.code || byte == '\t'.code) {
                            byte = input.read()
                        }
                        continue
                    }
                    '#'.code -> {
                        // Skip comments
                        while (byte != -1 && byte != '\r'.code && byte != '\n'.code) {
                            byte = input.read()
                        }
                        continue
                    }
                    '\r'.code, '\n'.code -> {
                        // Skip newlines
                        while (byte == '\r'.code || byte == '\n'.code) {
                            byte = input.read()
                        }
                        continue
                    }
                    '['.code -> {
                        // Parse section
                        bufferIndex = 0
                        byte = input.read() // skip [

                        // Handle section inheritance with [parent.child]
                        if (byte == '.'.code) {
                            for (c in section) {
                                buffer[bufferIndex++] = c.code.toByte()
                            }
                        }

                        while (byte != -1 && byte != ']'.code) {
                            buffer[bufferIndex++] = byte.toByte()
                            byte = input.read()
                        }

                        section = String(buffer, 0, bufferIndex)
                        byte = input.read() // skip ]
                    }
                    '"'.code -> {
                        // Parse quoted key
                        quotedString(input)
                        val key = String(buffer, 0, bufferIndex)
                        byte = input.read() // Skip closing quote

                        // Skip whitespace and equals sign
                        byte = skipKeyValueWhitespace(byte, input)

                        byte = parseValue(byte, input, section, key)
                    }
                    else -> {
                        byte = bareKey(byte, input)

                        val key = String(buffer, 0, bufferIndex)

                        // Skip whitespace and equals sign
                        byte = skipKeyValueWhitespace(byte, input)

                        byte = parseValue(byte, input, section, key)
                    }
                }
            }

            input.close()
        }

        private fun bareKey(currentByte: Int, input: BufferedInputStream): Int {
            bufferIndex = 0
            var byte = currentByte
            while (byte != -1 && byte != ' '.code && byte != '\t'.code && byte != '='.code && byte != '\r'.code && byte != '\n'.code) {
                buffer[bufferIndex++] = byte.toByte()
                byte = input.read()
            }
            return byte
        }

        private fun parseValue(byte: Int, input: BufferedInputStream, section: String, key: String): Int {
            var currentByte = byte

            // Skip leading whitespace
            while (currentByte == ' '.code || currentByte == '\t'.code) {
                currentByte = input.read()
            }

            val value: Any = parseType(currentByte, input)
            api.set(section, key, value)
            return input.read()
        }

        private fun parseType(currentByte: Int, input: BufferedInputStream): Any = when (currentByte) {
            '"'.code -> {
                quotedString(input)
                String(buffer, 0, bufferIndex)
            }
            '['.code -> parseArray(input)
            '{'.code -> parseMap(input)
            't'.code -> {
                parseTrue(input)
                true
            }
            'f'.code -> {
                parseFalse(input)
                false
            }
            '-'.code -> parseNumber(input, true, 0)
            '+'.code -> parseNumber(input, false, 0)
            '0'.code, '1'.code, '2'.code, '3'.code, '4'.code, '5'.code, '6'.code, '7'.code, '8'.code, '9'.code ->
                parseNumber(input, false, currentByte - '0'.code)
            else -> throw IllegalArgumentException("Unexpected character '${currentByte.toChar()}'")
        }

        private fun parseNumber(input: BufferedInputStream, negative: Boolean, initialDigit: Int): Number {
            var value = initialDigit.toLong()
            var byte = input.read()
            while (byte != -1 && byte != '.'.code && byte != '\r'.code && byte != '\n'.code && byte != ','.code && byte != '}'.code && byte != ']'.code) {
                when (byte) {
                    '_'.code -> {
                        // Skip underscores
                    }
                    '0'.code, '1'.code, '2'.code, '3'.code, '4'.code, '5'.code, '6'.code, '7'.code, '8'.code, '9'.code -> {
                        val digit = byte - '0'.code
                        value = value * 10 + digit
                    }
                    else -> break
                }
                byte = input.read()
            }
            if (byte == '.'.code) {
                var decimalFactor = 1.0
                var doubleValue = value.toDouble()
                byte = input.read() // Skip the dot
                while (byte != -1 && byte != '.'.code && byte != '\r'.code && byte != '\n'.code && byte != ','.code && byte != '}'.code && byte != ']'.code) {
                    when (byte) {
                        '_'.code -> {
                            // Skip underscores
                        }
                        '0'.code, '1'.code, '2'.code, '3'.code, '4'.code, '5'.code, '6'.code, '7'.code, '8'.code, '9'.code -> {
                            val digit = byte - '0'.code
                            decimalFactor /= 10
                            doubleValue += digit * decimalFactor
                        }
                        else -> break
                    }
                    byte = input.read()
                }
                return if (negative) -doubleValue else doubleValue
            }
            return if (negative) -value else value
        }

        private fun parseTrue(input: BufferedInputStream) {
            if (input.read() != 'r'.code && input.read() != 'u'.code && input.read() != 'e'.code) {
                throw IllegalArgumentException("Expected boolean true.")
            }
        }

        private fun parseFalse(input: BufferedInputStream) {
            if (input.read() != 'a'.code && input.read() != 'l'.code && input.read() != 's'.code && input.read() != 'e'.code) {
                throw IllegalArgumentException("Expected boolean false.")
            }
        }

        private fun parseMap(input: BufferedInputStream): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            var byte = input.read() // skip opening brace

            while (byte != -1 && byte != '}'.code) {
                // Skip whitespace and commas
                byte = skipMultilineWhitespace(byte, input)

                if (byte == '}'.code) {
                    break
                }

                byte = if (byte == '"'.code) {
                    quotedString(input)
                    input.read() // Skip closing quote
                } else {
                    bareKey(byte, input)
                }

                val mapKey = String(buffer, 0, bufferIndex).trim()

                // Skip whitespace and equals sign
                byte = skipKeyValueWhitespace(byte, input)

                // Parse map value
                bufferIndex = 0

                val value = parseType(byte, input)
                map[mapKey] = value
                byte = input.read()
            }

            return map
        }

        private fun skipKeyValueWhitespace(byte: Int, input: BufferedInputStream): Int {
            var byte1 = byte
            while (byte1 == ' '.code || byte1 == '\t'.code || byte1 == '='.code) {
                byte1 = input.read()
            }
            return byte1
        }

        private fun parseArray(input: BufferedInputStream): List<Any> {
            // Parse array [1, 2, 3]
            val values = mutableListOf<Any>()
            var byte = input.read() // skip opening bracket

            while (byte != -1 && byte != ']'.code) {
                // Skip whitespace and commas
                byte = skipMultilineWhitespace(byte, input)

                if (byte == ']'.code) {
                    break
                }

                val value = parseType(byte, input)
                values.add(value)
                byte = input.read()
            }
            return values
        }

        private fun skipMultilineWhitespace(byte: Int, input: BufferedInputStream): Int {
            var byte1 = byte
            while (byte1 == ' '.code || byte1 == '\t'.code || byte1 == ','.code || byte1 == '\r'.code || byte1 == '\n'.code) {
                byte1 = input.read()
            }
            return byte1
        }

        private fun quotedString(input: BufferedInputStream) {
            bufferIndex = 0
            var currentByte = input.read() // skip opening quote

            while (currentByte != -1 && currentByte != '"'.code) {
                if (currentByte == '\\'.code) {
                    currentByte = input.read()
                    if (currentByte == '"'.code) {
                        buffer[bufferIndex++] = '"'.code.toByte()
                        currentByte = input.read()
                        continue
                    } else {
                        buffer[bufferIndex++] = '\\'.code.toByte()
                    }
                }

                buffer[bufferIndex++] = currentByte.toByte()
                currentByte = input.read()
            }
        }
    }

    // Implementation of the Api interface
    class IniConfig : Api {
        val sections = mutableMapOf<String, MutableMap<String, Any>>()

        override fun set(section: String, key: String, value: String) {
            getOrCreateSection(section)[key] = value
        }

        override fun set(section: String, key: String, value: Long) {
            getOrCreateSection(section)[key] = value
        }

        override fun set(section: String, key: String, value: Double) {
            getOrCreateSection(section)[key] = value
        }

        override fun set(section: String, key: String, value: Any) {
            getOrCreateSection(section)[key] = value
        }

        override fun set(section: String, key: String, value: Boolean) {
            getOrCreateSection(section)[key] = value
        }

        override fun set(section: String, key: String, values: List<Any>) {
            getOrCreateSection(section)[key] = values
        }

        override fun set(section: String, key: String, values: Map<String, Any>) {
            getOrCreateSection(section)[key] = values
        }

        private fun getOrCreateSection(section: String): MutableMap<String, Any> {
            return sections.getOrPut(section) { mutableMapOf() }
        }

        // Helper methods to retrieve values
        fun getString(section: String, key: String, default: String = ""): String {
            val value = sections[section]?.get(key) ?: return default
            return value.toString()
        }

        fun getLong(section: String, key: String, default: Long = 0): Long {
            val value = sections[section]?.get(key) ?: return default
            return when (value) {
                is Long -> value
                is String -> value.toLongOrNull() ?: default
                else -> default
            }
        }

        fun getBoolean(section: String, key: String, default: Boolean = false): Boolean {
            val value = sections[section]?.get(key) ?: return default
            return when (value) {
                is Boolean -> value
                is String -> value.equals("true", ignoreCase = true)
                else -> default
            }
        }

        fun getList(section: String, key: String): List<String> {
            val value = sections[section]?.get(key) ?: return emptyList()
            return when (value) {
                is List<*> -> value.filterIsInstance<String>()
                else -> emptyList()
            }
        }

        fun getMap(section: String, key: String): Map<String, String> {
            val value = sections[section]?.get(key) ?: return emptyMap()
            return when (value) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    value as Map<String, String>
                }
                else -> emptyMap()
            }
        }
    }

    // Usage example
    /*fun main() {
        val config = IniConfig()
        val parser = IniParser(config)
        parser.parse(File("config.ini"))

        // Access values
        val serverPort = config.getLong("server", "port", 8080)
        val debugMode = config.getBoolean("debug", "enabled", false)
        val userList = config.getList("users", "admins")
    }*/
    @JvmStatic
    fun main(args: Array<String>) {
//        val file = File("./temp/toml/interfaces.toml")


        val filter = File("./temp/toml/").walkTopDown().filter { it.isFile && it.extension == "toml" }

        var buffer = ByteArray(1024)
        var bufferIndex = 0
        var start = System.currentTimeMillis()
        val config = IniConfig()
        val reader = IniParser(config)
        var section = ""
        for (file in filter) {
            println(file.name)
            reader.parse(file)
            println(config.sections)
            break
        }
        println(config.getLong("harralander_tar", "id"))

        println("Took ${System.currentTimeMillis() - start}ms")
    }
}