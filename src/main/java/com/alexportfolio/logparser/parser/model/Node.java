package com.alexportfolio.logparser.parser.model;

public sealed interface Node permits ObjectNode, ArrayNode, StringNode, MultilineNode, RefNode {}







