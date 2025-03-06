# Strict TOML

A stricter version of [TOML](https://toml.io/en/) with some v1.1 features that should've been released 4 years ago.


## Spec Changes

### Multi-line inline maps

Multi-line arrays are allowed so maps should be too, keep it consistent.

### No date support

Use strings, none of this RFC 3339. 

```toml
date = "1979-05-27T07:32:00Z" 
```

### No immutability rules

Anything goes so long as the types match.

### No scientific format

You're the scientist, convert your own strings.