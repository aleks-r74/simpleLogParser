# Log Parser with Tree Deduplication

This project parses a custom structured log format into a tree of typed nodes that can later be used for data analysis.

The original log format represents a tree of objects with four value types:
- a string
- an array of strings
- a nested object structure, which may contain any of the supported types
- an array of object structures

## Example Input

```
RootObject<1>
    name = "example"
    items[2] = [
        {
            ItemObject<10>
                id = "A1"
                details = {
                    DetailsObject<20>
                        value = "hello"
                }
        }
        {
            ItemObject<10>
                id = "A1"
                details = {
                    DetailsObject<20>
                        value = "hello"
                }
        }
    ]

    notes = ...
        first line of multiline text
        second line of multiline text

    nested = {
        ChildObject<2>
            key = "value"
    }
```

## Example Output
```text{15}
{
  "type": "RootObject",
  "name ": "example",
  "items": [
    {
      "type": "ItemObject",
      "id ": "A1",
      "details ": {
        "type": "DetailsObject",
        "value ": "hello"
      }
    },
    {
      "type": "ItemObject",
      "ref": "timestamp:RootObject.items[2][0]$"
    }
  ],
  "notes ": [
    "first line of multiline text",
    "second line of multiline text"
  ],
  "nested ": {
    "type": "ChildObject",
    "key ": "value"
  }
}
```

## How It Works

The parser operates in two stages:

1. **Tokenization**  
   Raw text is converted into tokens representing logical units used during parsing.

2. **Parsing**  
   Tokens are processed to reconstruct the original tree structure.

This separation improves readability, maintainability, and resilience. For example, the parser can safely ignore noise in the end of the log, such as loose lines that are not part of meaningful data.

## Duplicate Object Collapsing

One of the main features is duplicate object collapsing.

After parsing, the tree can be scanned for repeated `ObjectNode` branches:
- The first occurrence remains unchanged
- Subsequent identical branches are replaced with reference nodes pointing to the original

This reduces log size and improves readability without losing structural information.

Duplicate detection uses deep structural comparison:
- Only `ObjectNode` instances are replaceable
- Arrays are not replaced, but objects inside arrays can be replaced

## Referencing-Only Mode

The parser also supports a *referencing-only* mode:
- No collapsing is performed
- Duplicate nodes are still identified and marked with references

This helps highlight repeated structures while keeping the full tree intact.

## Output

The parsed tree can be converted into a `LinkedHashMap` and serialized to JSON.
- Field order is preserved
- All scalar values are treated as strings

## Use Cases
This project is useful for logs that:
- contain deeply nested object structures
- include repeated data across branches or entries
- need to be compacted for readability
- need to be transformed into machine-readable formats (e.g., JSON) for further analysis

For brief expalantion of tokenization and parsing algorithm, check Parsing.docx
