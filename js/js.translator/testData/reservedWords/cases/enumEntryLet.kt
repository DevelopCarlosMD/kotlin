package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

enum class Foo {
    let
}

fun box(): String {
    testNotRenamed("let", { Foo.let })

    return "OK"
}