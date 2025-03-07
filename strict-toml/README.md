# Strict TOML

A stricter version of [TOML](https://toml.io/en/) with some v1.1 features that should've been released 4 years ago.


## Spec Changes

### Table inheritance

Inherit previous tables with `.` prefix.

```toml
[databases.foo.server]
ip = "10.0.0.1"

[.user] # Goodbye [databases.foo.server.user]
name = "root"
password = "root"
```

### Multi-line inline maps

Multi-line arrays are allowed so maps should be too, keep it consistent.

### No date support

Just use strings, none of this RFC 3339 nonsense. 

```toml
date = "1979-05-27T07:32:00Z" 
```

### No immutability rules

Anything goes so long as the types are correct.

### No scientific format

You're the scientist, convert your own strings.

### No infinity or nan

Who uses those anyway?