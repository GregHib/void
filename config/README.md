# Groml - Greg's Really Obvious Minimal Language

A custom config language that sits in-between a simplified [TOML](https://toml.io/en/) or an extended [INI](https://en.wikipedia.org/wiki/INI_file) in terms of complexity and features.
It supports multi-line arrays and maps like TOML v1.1. but not multiline strings, dates, special floats and integers. Sections instead of tables and section inheritance like ini files.

Files use the `.toml` ending for syntax highlighting support.

# Usage

## Simplified

```kotlin
val sections: Map<String, Any> = Config.decodeFromFile(file)
```

## Performance

```kotlin
Config.fileReader(file).use { reader ->
    while (reader.nextSection()) {
        val section = reader.section()
        while (reader.nextPair()) {
            val key = reader.key()
            
            // TODO: Read your values using reader.string(), boolean(), int(), long(), double() etc... 
        }
    }
}
```

### Reading arrays

```kotlin
while (reader.nextElement()) {
    // TODO: Read your values using reader.string(), boolean(), int(), long(), double() etc... 
}
```

### Reading maps

```kotlin
while (reader.nextPair()) {
    val key = reader.key()
    
    // TODO: Read your values using reader.string(), boolean(), int(), long(), double() etc... 
}
```

# Basic Spec

## Comments

```toml
# This is a full-line comment
key = "value"  # This is a comment at the end of a line
another = "# This is not a comment"
```

## Strings

*Basic strings* use double quotes and can be escaped.

```toml
str1 = "I'm a string."
str2 = "You can \"quote\" me."
```

*Literal strings* use single quotes and can't be escaped.

```toml
path = 'C:\Users\nodejs\templates'
quoted = 'Tom "Dubs" Preston-Werner'
```

Multi-line strings are **not** supported. Use an array of single-line strings.

## Numbers

```toml
# integers
int1 = +99
int2 = 42
int3 = 0
int4 = -17

# fractional
float1 = +1.0
float2 = 3.1415
float3 = -0.01

# separators
float8 = 224_617.445_991_228
```

## Sections

Groml files are split up into sections.

```toml
[section-1]
key1 = "some string"
key2 = 123

[section-2]
key1 = "another string"
key2 = 456
```

Sections can inherit previous section names using the `.` prefix.

```toml
[animal.dog]
name = "Fido"

[.physical] # Equivilant to 'animal.dog.physical'
colour = "brown"
breed = "Pug"

[.owner] # Equivilant to 'animal.dog.owner'
name = "Tom"
```

## Arrays

Arrays use square brackets. Whitespace and lines are ignored.

```toml
intergers = [1, 2, 3]
colours = ["red", "yellow", "green"]
nested_mixed_array = [ [ 1, 2 ], ["a", "b", "c"] ]
```

Arrays can be multi-line

```toml
integers2 = [
  1, 2, 3
]

integers3 = [
  1,
  2, # this is ok
]
```

## Maps

Maps use curly braces. Whitespace and commas are also ignored.

```toml
point = { x = 1, y = 2 }
animal = { type.name = "pug" }
```

Map can also be multi-line

```ini
point = {
  x = 1,
  y = 2, # also ok
}
animal = {
  type.name = "pug"
}
```

