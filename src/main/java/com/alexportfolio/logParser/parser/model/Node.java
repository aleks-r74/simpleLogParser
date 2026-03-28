package com.alexportfolio.logParser.parser.model;

public sealed interface Node permits ObjectNode, ArrayNode, StringNode, MultilineNode, RefNode {}







