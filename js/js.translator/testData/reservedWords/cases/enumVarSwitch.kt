package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

enum class Foo {
    BAR
    var switch: Int = 0

    fun test() {
        testNotRenamed("switch", { switch })
    }
}

fun box(): String {
    Foo.BAR.test()

    return "OK"
}