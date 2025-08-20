package world.gregs.config
// processor/SerializableProcessor.kt
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Serializable

class SerializableProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Serializable::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        for (symbol in symbols) {
            generateCodec(symbol)
        }
        return emptyList()
    }

    private fun generateCodec(classDecl: KSClassDeclaration) {
        val className = classDecl.simpleName.asString()
        val pkg = classDecl.packageName.asString()
        val fileName = "${className}Codec"

        val typeName = classDecl.toClassName()
        val fileSpec = FileSpec.builder(pkg, fileName)
            .addImport(ClassName("world.gregs", "config"), "Config", "map", "writePair", "writeKey", "writeValue", "list", "writeSection")
            .addType(
                TypeSpec.objectBuilder(fileName)
                    .addFunction(
                        FunSpec.builder("write")
                            .addParameter("writer", ClassName("world.gregs.config", "ConfigWriter"))
                            .addParameter("obj", typeName)
                            .addCode(buildSaveBody(classDecl, false))
                            .build(),
                    )
                    .addFunction(
                        FunSpec.builder("writeInline")
                            .addParameter("writer", ClassName("world.gregs.config", "ConfigWriter"))
                            .addParameter("obj", typeName)
                            .addCode(buildSaveBody(classDecl, true))
                            .build(),
                    )
                    .addFunction(
                        FunSpec.builder("read")
                            .addParameter("reader", ClassName("world.gregs.config", "ConfigReader"))
                            .returns(typeName)
                            .addCode(buildLoadBody(classDecl, false))
                            .build(),
                    )
                    .addFunction(
                        FunSpec.builder("readInline")
                            .addParameter("reader", ClassName("world.gregs.config", "ConfigReader"))
                            .returns(typeName)
                            .addCode(buildInlineBody(classDecl, true))
                            .build(),
                    )
                    .build(),
            )
            .build()

        codeGenerator.createNewFile(
            Dependencies(false, classDecl.containingFile!!),
            pkg,
            fileName,
        ).writer().use { out -> fileSpec.writeTo(out) }
    }

    private fun buildSaveBody(classDecl: KSClassDeclaration, inline: Boolean): CodeBlock {
        val builder = CodeBlock.builder()
        var first = true
        for (prop in classDecl.getAllProperties()) {
            val name = prop.simpleName.asString()
            val type = prop.type.resolve()
            if (first) {
                first = false
            } else if (inline) {
                builder.addStatement("    writer.write(\", \")", name)
            }
            first = false
            when (type.declaration.qualifiedName?.asString()) {
                "kotlin.Array", "kotlin.collections.List" -> {
                    val elementType = type.arguments.first().type!!.resolve()
                    builder.addStatement("    writer.writeKey(%S)", name)
                    builder.addStatement("    writer.list(obj.%L.size) { index -> ", name)
                    generateSerializationCode(builder, elementType, "obj.$name[index]", 1, inline)
                    builder.addStatement("    }", name)
                    if (!inline) {
                        builder.addStatement("    writer.write(\"\\n\")")
                    }
                }
                "kotlin.String", "kotlin.Boolean", "kotlin.Int", "kotlin.Long", "kotlin.Float", "kotlin.Double", "kotlin.IntArray", "kotlin.DoubleArray" -> {
                    builder.addStatement("    writer.writeKey(%S)", name)
                    builder.addStatement("    writer.writeValue(obj.%L)", name)
                    if (!inline) {
                        builder.addStatement("    writer.write(\"\\n\")")
                    }
                }
                "kotlin.collections.Map" -> {
                    val keyType = type.arguments.first().type!!.resolve()
                    val valueType = type.arguments[1].type!!.resolve()
                    if (inline) {
                        builder.addStatement("    writer.writeKey(%S)", name)
                        builder.addStatement("    writer.map(obj.%L.keys) { key0 -> ", name)
                        builder.addStatement("        val value0 = obj.$name.getValue(key0)", name)
                        generateSerializationCode(builder, valueType, "value0", 1, inline)
                        builder.addStatement("    }", name)
                    } else {
                        builder.addStatement("    writer.write(\"\\n\")")
                        builder.addStatement("    writer.writeSection(%S)", name)
                        builder.addStatement("    for ((k0, v0) in obj.%L) {", name)
                        builder.addStatement("        writer.writeKey(k0)")
                        generateSerializationCode(builder, valueType, "v0", 1, inline)
                        if (!inline) {
                            builder.addStatement("        writer.write(\"\\n\")")
                        }
                        builder.addStatement("    }")
//                        builder.addStatement("    writer.write(\"\\n\")")
                    }
                }
                else -> {
                    // Assume nested @Serializable object
                    if (!inline) {
                        builder.addStatement("    writer.write(\"\\n\")")
                        builder.addStatement("    writer.writeSection(%S)", name)
                    }
                    builder.addStatement(
                        "    %T.writeInline(writer, obj.%L)",
                        ClassName(
                            type.declaration.packageName.asString(),
                            type.declaration.simpleName.asString() + "Codec",
                        ),
                        name,
                    )
                    if (!inline) {
                        builder.addStatement("    writer.write(\"\\n\")")
                    }
                }
            }
        }
        return builder.build()
    }

    private fun generateSerializationCode(builder: CodeBlock.Builder, type: KSType, variable: String, depth: Int, inline: Boolean = false) {
        val indent = "    ".repeat(depth + 1)
        when (type.declaration.qualifiedName?.asString()) {
            "kotlin.String" -> {
                builder.addStatement("${indent}writer.write(\"\\\"\")")
                builder.addStatement("${indent}writer.write($variable)")
                builder.addStatement("${indent}writer.write(\"\\\"\")")
            }
            "kotlin.Boolean", "kotlin.Int", "kotlin.Long", "kotlin.Float", "kotlin.Double" -> {
                builder.addStatement("${indent}writer.writeValue($variable)")
            }
            "kotlin.Array", "kotlin.collections.List" -> {
                val elementType = type.arguments.firstOrNull()?.type!!.resolve()
                builder.addStatement("${indent}writer.list($variable.size) { idx$depth ->")
                generateSerializationCode(builder, elementType, variable = "$variable[idx$depth]", depth + 1)
                builder.addStatement("$indent}")
            }
            "kotlin.IntArray" -> {
                builder.addStatement("${indent}writer.list($variable.size) { idx$depth -> $variable[idx$depth] }")
            }
            "kotlin.collections.Map" -> {
                val keyType = type.arguments.first().type!!.resolve()
                val valueType = type.arguments[1].type!!.resolve()
                builder.addStatement("${indent}writer.map($variable.keys) { key$depth -> ")
                builder.addStatement("$indent    val value$depth = $variable.getValue(key$depth)")
                generateSerializationCode(builder, valueType, "value$depth", depth + 1, inline)
                builder.addStatement("$indent}")
            }
            else -> {
                if ((type.declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS) {
                    builder.addStatement("${indent}writer.write(\"\\\"\")")
                    builder.addStatement("${indent}writer.write($variable.name)")
                    builder.addStatement("${indent}writer.write(\"\\\"\")")
                } else {
                    // Assume nested @Serializable object
                    builder.addStatement("${indent}writer.write(\"{\")")
                    builder.addStatement(
                        "$indent%T.writeInline(writer, $variable)",
                        ClassName(
                            type.declaration.packageName.asString(),
                            type.declaration.simpleName.asString() + "Codec",
                        ),
                    )
                    builder.addStatement("${indent}writer.write(\"}\")")
                }
            }
        }
    }

    private fun buildLoadBody(classDecl: KSClassDeclaration, inline: Boolean): CodeBlock {
        val builder = CodeBlock.builder()

        // Vars for properties
        for (prop in classDecl.getAllProperties()) {
            val name = prop.simpleName.asString()
            val type = prop.type.resolve()
            when (type.declaration.qualifiedName?.asString()) {
                "kotlin.String" -> builder.addStatement("var %L: String = \"\"", name)
                "kotlin.Boolean" -> builder.addStatement("var %L: Boolean = false", name)
                "kotlin.Int" -> builder.addStatement("var %L: Int = 0", name)
                "kotlin.Long" -> builder.addStatement("var %L: Long = 0L", name)
                "kotlin.Double" -> builder.addStatement("var %L: Double = 0.0", name)
                "kotlin.Array" -> builder.addStatement("val %L = mutableListOf<%T>()", name, prop.type.resolve().arguments.first().type?.resolve()?.toTypeName())
                "kotlin.IntArray" -> builder.addStatement("val %L = mutableListOf<Int>()", name)
                "kotlin.collections.List" -> builder.addStatement("val %L = mutableListOf<%T>()", name, prop.type.resolve().arguments.first().type?.resolve()?.toTypeName())
                "kotlin.collections.Map" -> {
                    val arguments = prop.type.resolve().arguments
                    val keyType = arguments.first().type?.resolve()?.toTypeName()
                    val valueType = arguments.last().type?.resolve()?.toTypeName()
                    builder.addStatement("val %L = mutableMapOf<%T, %T>()", name, keyType, valueType)
                }
                else -> builder.addStatement("lateinit var %L: %T", name, type.toTypeName())
            }
        }

        // read sections
        // if blank readPairs

        // inline -> read value
        var indent = 0
        if (!inline) {
            builder.addStatement("while (reader.nextSection()) {")
            builder.addStatement("    val section = reader.section()")
            builder.addStatement("    println(\"section: \$section\")")
            builder.addStatement("    when (section) {")
            for (prop in classDecl.getAllProperties()) {
                val name = prop.simpleName.asString()
                val type = prop.type.resolve()
                when (type.declaration.qualifiedName?.asString()) {
                    "kotlin.String",
                    "kotlin.Boolean", "kotlin.Int", "kotlin.Long",
                    "kotlin.Double", "kotlin.Array", "kotlin.collections.List", "kotlin.IntArray",
                        -> {
                    }
                    "kotlin.collections.Map" -> { // Map or objects
                        val keyType = type.arguments.firstOrNull()?.type?.resolve()
                        val valueType = type.arguments.getOrNull(1)?.type?.resolve()
                        builder.addStatement("        %S -> while (reader.nextPair()) {", name)
                        builder.addStatement("            val key0 = reader.key()")
                        builder.addStatement("            println(\"key: \$key0\")")
                        generateDeserializationCode(builder, valueType!!, 0)
                        builder.addStatement("            %L[key0] = value0", name)
                        builder.addStatement("        }")
                    }
                    else -> {
                        val codecClassName = ClassName(
                            type.declaration.packageName.asString(),
                            type.declaration.simpleName.asString() + "Codec",
                        )
                        builder.addStatement("        %S -> %L = %T.readInline(reader)", name, name, codecClassName)
                    }
                }
            }
            builder.addStatement("        \"\" -> while (reader.nextPair()) {")
            indent = 4
        } else {
            builder.addStatement("while (reader.nextEntry()) {") //Objects are entries
            indent++
        }
        builder.addStatement("${"    ".repeat(indent)}val key = reader.key()")
        builder.addStatement("${"    ".repeat(indent)}println(\"k: \$key\")")
        builder.addStatement("${"    ".repeat(indent)}when (key) {")
        indent++
        for (prop in classDecl.getAllProperties()) {
            val name = prop.simpleName.asString()
            val type = prop.type.resolve()
            when (type.declaration.qualifiedName?.asString()) {
                "kotlin.String" -> builder.addStatement("${"    ".repeat(indent)}%S -> %L = reader.string()", name, name)
                "kotlin.Boolean" -> builder.addStatement("${"    ".repeat(indent)}%S -> %L = reader.boolean()", name, name)
                "kotlin.Int" -> builder.addStatement("${"    ".repeat(indent)}%S -> %L = reader.int()", name, name)
                "kotlin.Long" -> builder.addStatement("${"    ".repeat(indent)}%S -> %L = reader.long()", name, name)
                "kotlin.Double" -> builder.addStatement("${"    ".repeat(indent)}%S -> %L = reader.double()", name, name)
                "kotlin.Array", "kotlin.collections.List" -> {
                    builder.addStatement("${"    ".repeat(indent)}%S -> ", name)
                    val elementType = type.arguments.first().type?.resolve()
                    generateListDeserializationCode(builder, name, elementType)
                }
                "kotlin.IntArray" -> {
                    builder.addStatement("${"    ".repeat(indent)}%S -> while (reader.nextElement()) { %L.add(reader.int()) }", name, name)
                }
                "kotlin.collections.Map" -> {
                    if (inline) {
                        val keyType = type.arguments.firstOrNull()?.type?.resolve()
                        val valueType = type.arguments.getOrNull(1)?.type?.resolve()
                        builder.addStatement("        %S -> while (reader.nextEntry()) {", name)
                        builder.addStatement("            val key0 = reader.key()")
                        generateDeserializationCode(builder, valueType!!, 0)
                        builder.addStatement("            %L[key0] = value0", name)
                        builder.addStatement("        }")
                    }
                }
                else -> {
                    if ((type.declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS) {
                        builder.addStatement(
                            "${"    ".repeat(indent)}%S -> %L = %T.valueOf(reader.string().uppercase())", name, name,
                            ClassName(
                                type.declaration.packageName.asString(),
                                type.declaration.simpleName.asString(),
                            ),
                        )
                    } else {
                        val codecClassName = ClassName(
                            type.declaration.packageName.asString(),
                            type.declaration.simpleName.asString() + "Codec",
                        )
                        builder.addStatement("${"    ".repeat(indent)}%S -> %L = %T.readInline(reader)", name, name, codecClassName)
                    }
                }
            }
        }
        builder.addStatement("${"    ".repeat(indent--)}else -> throw IllegalArgumentException(\"Unexpected key: '${"$"}key'\")")
        builder.addStatement("${"    ".repeat(indent--)}}")
        builder.addStatement("${"    ".repeat(indent)}}")
        if (!inline) {
            indent--
            builder.addStatement("${"    ".repeat(indent--)}else -> throw IllegalArgumentException(\"Unexpected section: '${"$"}section'\")")
            builder.addStatement("${"    ".repeat(indent)}}")
            builder.addStatement("}")
        }
//
//        // Construct and return instance
        builder.add("return %T(", classDecl.toClassName())
        builder.add(
            classDecl.getAllProperties().joinToString(", ") {
                when (it.type.resolve().declaration.qualifiedName?.asString()) {
                    "kotlin.IntArray" -> "${it.simpleName.asString()} = ${it.simpleName.asString()}.toIntArray()"
                    else -> "${it.simpleName.asString()} = ${it.simpleName.asString()}"
                }
            },
        )
        builder.addStatement(")")

        return builder.build()
    }

    private fun buildInlineBody(classDecl: KSClassDeclaration, inline: Boolean): CodeBlock {
        val builder = CodeBlock.builder()

        // Vars for properties
        for (prop in classDecl.getAllProperties()) {
            val name = prop.simpleName.asString()
            val type = prop.type.resolve()
            when (type.declaration.qualifiedName?.asString()) {
                "kotlin.String" -> builder.addStatement("var %L: String = \"\"", name)
                "kotlin.Boolean" -> builder.addStatement("var %L: Boolean = false", name)
                "kotlin.Int" -> builder.addStatement("var %L: Int = 0", name)
                "kotlin.Long" -> builder.addStatement("var %L: Long = 0L", name)
                "kotlin.Double" -> builder.addStatement("var %L: Double = 0.0", name)
                "kotlin.Array" -> builder.addStatement("val %L = mutableListOf<%T>()", name, prop.type.resolve().arguments.first().type?.resolve()?.toTypeName())
                "kotlin.IntArray" -> builder.addStatement("val %L = mutableListOf<Int>()", name)
                "kotlin.collections.List" -> builder.addStatement("val %L = mutableListOf<%T>()", name, prop.type.resolve().arguments.first().type?.resolve()?.toTypeName())
                "kotlin.collections.Map" -> {
                    val arguments = prop.type.resolve().arguments
                    val keyType = arguments.first().type?.resolve()?.toTypeName()
                    val valueType = arguments.last().type?.resolve()?.toTypeName()
                    builder.addStatement("val %L = mutableMapOf<%T, %T>()", name, keyType, valueType)
                }
                else -> builder.addStatement("lateinit var %L: %T", name, type.toTypeName())
            }
        }

        // read sections
        // if blank readPairs

        // inline -> read value
        var indent = 1
        builder.addStatement("while (reader.nextPair()) {")
        builder.addStatement("${"    ".repeat(indent++)}when (val key = reader.key()) {")
        for (prop in classDecl.getAllProperties()) {
            val name = prop.simpleName.asString()
            val type = prop.type.resolve()
            when (type.declaration.qualifiedName?.asString()) {
                "kotlin.String" -> builder.addStatement("${"    ".repeat(indent)}%S -> %L = reader.string()", name, name)
                "kotlin.Boolean" -> builder.addStatement("${"    ".repeat(indent)}%S -> %L = reader.boolean()", name, name)
                "kotlin.Int" -> builder.addStatement("${"    ".repeat(indent)}%S -> %L = reader.int()", name, name)
                "kotlin.Long" -> builder.addStatement("${"    ".repeat(indent)}%S -> %L = reader.long()", name, name)
                "kotlin.Double" -> builder.addStatement("${"    ".repeat(indent)}%S -> %L = reader.double()", name, name)
                "kotlin.Array", "kotlin.collections.List" -> {
                    val elementType = type.arguments.first().type?.resolve()!!
                    builder.addStatement("${"    ".repeat(indent)}%S -> while (reader.nextElement()) {", name)
                    generateDeserializationCode(builder, elementType, 0)
                    builder.addStatement("${"    ".repeat(indent)}    %L.add(value0)", name)
                    builder.addStatement("${"    ".repeat(indent)}}", name)
                }
                "kotlin.IntArray" -> {
                    builder.addStatement("${"    ".repeat(indent)}%S -> while (reader.nextElement()) { %L.add(reader.int()) }", name, name)
                }
                "kotlin.collections.Map" -> {
                    if (inline) {
                        val keyType = type.arguments.firstOrNull()?.type?.resolve()
                        val valueType = type.arguments.getOrNull(1)?.type?.resolve()
                        builder.addStatement("        %S -> while (reader.nextEntry()) {", name)
                        builder.addStatement("            val key0 = reader.key()")
                        generateDeserializationCode(builder, valueType!!, 0)
                        builder.addStatement("            %L[key0] = value0", name)
                        builder.addStatement("        }")
                    }
                }
                else -> {
                    if ((type.declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS) {
                        builder.addStatement(
                            "${"    ".repeat(indent)}%S -> %L = %T.valueOf(reader.string().uppercase())", name, name,
                            ClassName(
                                type.declaration.packageName.asString(),
                                type.declaration.simpleName.asString(),
                            ),
                        )
                    } else {
                        val codecClassName = ClassName(
                            type.declaration.packageName.asString(),
                            type.declaration.simpleName.asString() + "Codec",
                        )
                        builder.addStatement("${"    ".repeat(indent)}%S -> %L = %T.readInline(reader)", name, name, codecClassName)
                    }
                }
            }
        }
        builder.addStatement("${"    ".repeat(indent)}else -> throw IllegalArgumentException(\"Unexpected key: '${"$"}key'\")")
        builder.addStatement("${"    ".repeat(--indent)}}")
        builder.addStatement("}")
//
//        // Construct and return instance
        builder.add("return %T(", classDecl.toClassName())
        builder.add(
            classDecl.getAllProperties().joinToString(", ") {
                when (it.type.resolve().declaration.qualifiedName?.asString()) {
                    "kotlin.IntArray" -> "${it.simpleName.asString()} = ${it.simpleName.asString()}.toIntArray()"
                    else -> "${it.simpleName.asString()} = ${it.simpleName.asString()}"
                }
            },
        )
        builder.addStatement(")")

        return builder.build()
    }

    private fun generateDeserializationCode(builder: CodeBlock.Builder, type: KSType, depth: Int) {
        val indent = "    ".repeat(depth + 3)
        when (type.declaration.qualifiedName?.asString()) {
            "kotlin.String" -> builder.addStatement("${indent}val value$depth = reader.string()")
            "kotlin.Int" -> builder.addStatement("${indent}val value$depth = reader.int()")
            "kotlin.Long" -> builder.addStatement("${indent}val value$depth = reader.long()")
            "kotlin.Double" -> builder.addStatement("${indent}val value$depth = reader.double()")
            "kotlin.Boolean" -> builder.addStatement("${indent}val value$depth = reader.boolean()")
            "kotlin.collections.Map" -> {
                // Handle List<Map<K, V>>
                val keyType = type.arguments.firstOrNull()?.type?.resolve()
                val valueType = type.arguments.getOrNull(1)?.type?.resolve()
                builder.addStatement("${indent}val value$depth = mutableMapOf<%T, %T>()", keyType?.toTypeName(), valueType?.toTypeName())
                builder.addStatement("${indent}while (reader.nextElement()) {")
                builder.addStatement("$indent    val key${depth + 1} = reader.key()")
                generateDeserializationCode(builder, valueType!!, depth + 1)
//                builder.addStatement("    value${depth}[key${depth}] = value${depth}", name)
                builder.addStatement("$indent    value$depth[key${depth + 1}] = value${depth + 1}")
                builder.addStatement("$indent}")
            }
            "kotlin.collections.List" -> {
                // Handle List<List<T>>
//                val nestedElementType = elementType.arguments.firstOrNull()?.type?.resolve()
//                builder.addStatement("while (reader.nextElement()) {")
//                builder.addStatement(
//                    "    val listItem = mutableListOf<%T>()",
//                    nestedElementType?.toTypeName(),
//                )
//                generateListDeserializationCode(builder, "listItem", nestedElementType)
//                builder.addStatement("    %L.add(listItem)", name)
//                builder.addStatement("}")
            }
            else -> {
                // Custom type - use its codec
                if ((type.declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS) {
                    builder.addStatement(
                        "${indent}val value$depth = %T.valueOf(reader.string().uppercase())",
                        ClassName(
                            type.declaration.packageName.asString(),
                            type.declaration.simpleName.asString(),
                        ),
                    )
                } else {
                    val codecClassName = ClassName(
                        type.declaration.packageName.asString(),
                        type.declaration.simpleName.asString() + "Codec",
                    )
                    builder.addStatement("${indent}val value$depth = %T.readInline(reader)", codecClassName)
                }
            }
        }
    }

    private fun generateListDeserializationCode(builder: CodeBlock.Builder, name: String, elementType: KSType?) {
        when (elementType?.declaration?.qualifiedName?.asString()) {
            "kotlin.String" -> {
                builder.addStatement("while (reader.nextElement()) { %L.add(reader.string()) }", name)
            }
            "kotlin.Int" -> {
                builder.addStatement("while (reader.nextElement()) { %L.add(reader.int()) }", name)
            }
            "kotlin.Long" -> {
                builder.addStatement("while (reader.nextElement()) { %L.add(reader.long()) }", name)
            }
            "kotlin.Double" -> {
                builder.addStatement("while (reader.nextElement()) { %L.add(reader.double()) }", name)
            }
            "kotlin.Boolean" -> {
                builder.addStatement("while (reader.nextElement()) { %L.add(reader.boolean()) }", name)
            }
            "kotlin.collections.Map" -> {
                // Handle List<Map<K, V>>
                val keyType = elementType.arguments.firstOrNull()?.type?.resolve()
                val valueType = elementType.arguments.getOrNull(1)?.type?.resolve()
                builder.addStatement("while (reader.nextElement()) {")
                builder.addStatement(
                    "    val mapItem = mutableMapOf<%T, %T>()",
                    keyType?.toTypeName(),
                    valueType?.toTypeName(),
                )
                generateMapDeserializationCode(builder, "mapItem", keyType, valueType, "    ")
                builder.addStatement("    %L.add(mapItem)", name)
                builder.addStatement("}")
            }
            "kotlin.collections.List" -> {
                // Handle List<List<T>>
                val nestedElementType = elementType.arguments.firstOrNull()?.type?.resolve()
                builder.addStatement("while (reader.nextElement()) {")
                builder.addStatement(
                    "    val listItem = mutableListOf<%T>()",
                    nestedElementType?.toTypeName(),
                )
                generateListDeserializationCode(builder, "listItem", nestedElementType)
                builder.addStatement("    %L.add(listItem)", name)
                builder.addStatement("}")
            }
            else -> {
                // Custom type - use its codec
                val codecClassName = ClassName(
                    elementType?.declaration?.packageName?.asString() ?: "",
                    (elementType?.declaration?.simpleName?.asString() ?: "Unknown") + "Codec",
                )
                builder.addStatement(
                    "while (reader.nextElement()) { %L.add(%T.readInline(reader)) }",
                    name,
                    codecClassName,
                )
            }
        }
    }

    private fun generateMapDeserializationCode(
        builder: CodeBlock.Builder,
        name: String,
        keyType: KSType?,
        valueType: KSType?,
        indent: String = "",
    ) {
        val keyReader = when (keyType?.declaration?.qualifiedName?.asString()) {
            "kotlin.String" -> "reader.key()"
            "kotlin.Int" -> "reader.key().toInt()"
            "kotlin.Long" -> "reader.key().toLong()"
            else -> "reader.key()" // Default to string key
        }

        when (valueType?.declaration?.qualifiedName?.asString()) {
            "kotlin.String" -> {
                builder.addStatement(
                    "${indent}while (reader.nextEntry()) { %L[%L] = reader.string() }",
                    name,
                    keyReader,
                )
            }
            "kotlin.Int" -> {
                builder.addStatement(
                    "${indent}while (reader.nextEntry()) { %L[%L] = reader.int() }",
                    name,
                    keyReader,
                )
            }
            "kotlin.Long" -> {
                builder.addStatement(
                    "${indent}while (reader.nextEntry()) { %L[%L] = reader.long() }",
                    name,
                    keyReader,
                )
            }
            "kotlin.Double" -> {
                builder.addStatement(
                    "${indent}while (reader.nextEntry()) { %L[%L] = reader.double() }",
                    name,
                    keyReader,
                )
            }
            "kotlin.Boolean" -> {
                builder.addStatement(
                    "${indent}while (reader.nextEntry()) { %L[%L] = reader.boolean() }",
                    name,
                    keyReader,
                )
            }
            "kotlin.collections.Map" -> {
                // Handle Map<K, Map<K2, V2>> - nested maps
                val nestedKeyType = valueType.arguments.firstOrNull()?.type?.resolve()
                val nestedValueType = valueType.arguments.getOrNull(1)?.type?.resolve()
                builder.addStatement("${indent}while (reader.nextEntry()) {")
                builder.addStatement(
                    "$indent    val nestedMap = mutableMapOf<%T, %T>()",
                    nestedKeyType?.toTypeName(),
                    nestedValueType?.toTypeName(),
                )
                generateMapDeserializationCode(builder, "nestedMap", nestedKeyType, nestedValueType, "$indent    ")
                builder.addStatement("$indent    %L[%L] = nestedMap", name, keyReader)
                builder.addStatement("$indent}")
            }
            "kotlin.collections.List" -> {
                // Handle Map<K, List<T>>
                val listElementType = valueType.arguments.firstOrNull()?.type?.resolve()
                builder.addStatement("${indent}while (reader.nextEntry()) {")
                builder.addStatement(
                    "$indent    val listValue = mutableListOf<%T>()",
                    listElementType?.toTypeName(),
                )
                generateListDeserializationCode(builder, "listValue", listElementType)
                builder.addStatement("$indent    %L[%L] = listValue", name, keyReader)
                builder.addStatement("$indent}")
            }
            else -> {
                // Custom type - use its codec
                val codecClassName = ClassName(
                    valueType?.declaration?.packageName?.asString() ?: "",
                    (valueType?.declaration?.simpleName?.asString() ?: "Unknown") + "Codec",
                )
                builder.addStatement("{}")
//                builder.addStatement(
//                    "${indent}while (reader.nextEntry()) { %L[%L] = %T.read(reader) }",
//                    name, keyReader, codecClassName
//                )
            }
        }
    }
}
