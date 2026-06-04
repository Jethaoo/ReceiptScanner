package com.example.receiptscanner.data

fun Iterable<String>.toTagsStorage(): String =
    filter { it.isNotBlank() }.joinToString(",")

fun String.toTagList(): List<String> =
    split(",").map { it.trim() }.filter { it.isNotEmpty() }
