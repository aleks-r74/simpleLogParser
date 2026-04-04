# Log Parser with Tree Deduplication

This project parses a custom structured log format into a tree of typed nodes that can later be used for data analysis.

The original log format represents a tree of objects with four value types:
- a string
- an array of strings
- a nested object structure, which may contain any supported type
- an array of object structures

## Example Input

```text
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

```json
{
  "type": "RootObject",
  "name": "example",
  "items": [
    {
      "type": "ItemObject",
      "metadata": {
        "id": "timestamp.RootObject.items[0]"
      },
      "id": "A1",
      "details": {
        "type": "DetailsObject",
        "value": "hello"
      }
    },
    {
      "type": "ItemObject",
      "ref": "timestamp.RootObject.items[0]"
    }
  ],
  "notes": [
    "first line of multiline text",
    "second line of multiline text"
  ],
  "nested": {
    "type": "ChildObject",
    "key": "value"
  }
}
```

## How It Works

The parser works in 3 stages:

1. **Tokenization**  
   The raw input text is converted into structure tokens - these are the smallest atomic units that the parser will work with. Structure tokens capture basic elements such as brackets, line breaks, and text fragments without imposing any grammar rules.


2. **PostProcessing**
   Structure tokens are transformed into grammar tokens, which carry semantic meaning according to your language’s rules. This stage also handles contextual logic, such as detecting multi-line blocks, skipping the first EOL after a block starts, and grouping lines into meaningful units.

2. **Parsing**  
   The parser consumes grammar tokens to build the final in-memory representation. At this stage, the logic is simple because all the contextual work has already been handled in the previous stages.

This separation improves readability, maintainability, and resilience. For example, the parser can safely ignore noise at the end of the log, such as loose lines that are not part of the meaningful data.

## Duplicate Object Collapsing

One of the main features is duplicate object collapsing.

After parsing, the tree can be scanned for repeated `Objects` in branches:
- the first occurrence remains unchanged
- later identical branches are replaced with reference nodes pointing to the original

This reduces log size and improves readability without losing structural information.


## Referencing-Only Mode

The parser also supports a *referencing-only* mode:
- no collapsing is performed
- duplicate nodes are still identified and marked with references

This helps highlight repeated structures while keeping the full tree intact.

## Output

The parsed tree can be converted into a `LinkedHashMap` and serialized to JSON.

- field order is preserved
- all scalar values are treated as strings

## Use Cases

This project is useful for logs that:
- contain deeply nested object structures
- include repeated data across branches or entries
- need to be compacted for readability
- need to be transformed into machine-readable formats such as JSON for further analysis

For a brief explanation of the tokenization and parsing algorithm, see `Parsing.docx`.