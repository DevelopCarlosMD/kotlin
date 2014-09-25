/*
 * Copyright 2010-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.generators.tests.reservedWords

import java.io.File
import org.jetbrains.jet.renderer.KeywordStringsGenerated
import com.google.dart.compiler.backend.js.ast.JsFunctionScope

val commonCases: CaseBuilder.(String, String) -> Unit = { (testByName, testByRef) ->
    case("val", "val $KEYWORD_MARKER: Int", " = 0", testByName)
    case("var", "var $KEYWORD_MARKER: Int", " = 0", testByName)
    case("fun", "fun $KEYWORD_MARKER()", " { $KEYWORD_MARKER() }", testByRef)
    case("funParam",
         "fun foo($KEYWORD_MARKER: String)", """ {
    assertEquals("123", $KEYWORD_MARKER)
    $testRenamedByName
}""", "foo(\"123\")")

    case("label", "val t: Int", " = 0", "testRenamed(\"$KEYWORD_MARKER\", { @$KEYWORD_MARKER while (false) {} })")
}

val commonCasesWithTestNotRenamed: CaseBuilder.() -> Unit = { commonCases(testNotRenamedByName, testNotRenamedByRef) }

fun main(args: Array<String>) {
    generateTestDataForReservedWords()
}

fun generateTestDataForReservedWords() {
    generate("js/js.translator/testData/reservedWords/cases") {
        suite("toplevel",
"""
$DEFINITION_MARKER

fun box(): String {
    $TEST_BLOCK_MARKER

    return "OK"
}"""
        ) {
            commonCasesWithTestNotRenamed()

            case("class", "class $KEYWORD_MARKER { class object {} }", "", testNotRenamedByName)
            case("trait", "trait $KEYWORD_MARKER { class object {} }", "", testNotRenamedByName)
            case("enum", "enum class $KEYWORD_MARKER { foo }", "", testNotRenamed("$KEYWORD_MARKER.foo"))
            case("object", "object $KEYWORD_MARKER {}", "", testNotRenamedByName)
        }
// -------------------------

        suite("local",
 """
fun box(): String {
    $DEFINITION_MARKER

    $TEST_BLOCK_MARKER

    return "OK"
}"""
        ) {
            commonCases(testRenamedByName, testRenamedByRef)
            case("catch", "", "",
"""
    try {
        throw Exception()
    }
    catch($KEYWORD_MARKER: Exception) {
        $testRenamedByName
    }""")

        }
// -------------------------

        suite("insideClass",
"""
class TestClass {
    $DEFINITION_MARKER

    fun test() {
        $TEST_BLOCK_MARKER
    }
}

fun box(): String {
    TestClass().test()

    return "OK"
}""", commonCasesWithTestNotRenamed)
// -------------------------

        suite("insideClassObject",
"""
class TestClass {
    class object {
        $DEFINITION_MARKER

        fun test() {
            $TEST_BLOCK_MARKER
        }
    }
}

fun box(): String {
    TestClass.test()

    return "OK"
}""", commonCasesWithTestNotRenamed)
// -------------------------

        suite("insideObject",
"""
object TestObject {
    $DEFINITION_MARKER

    fun test() {
        $TEST_BLOCK_MARKER
    }
}

fun box(): String {
    TestObject.test()

    return "OK"
}""", commonCasesWithTestNotRenamed)
// -------------------------

        suite("dataClass",
"""
data class DataClass($DEFINITION_MARKER: String) {
    {
        $TEST_BLOCK_MARKER
    }
}

fun box(): String {
    DataClass("123")

    return "OK"
}"""
        ) {
            case("val", "val $KEYWORD_MARKER", "", testNotRenamedByName)
            case("var", "var $KEYWORD_MARKER", "", testNotRenamedByName)
            case("param", "$KEYWORD_MARKER",  "",testRenamedByName)
        }
// -------------------------

        suite("delegated",
"""
trait Trait {
    $DECLARATION_MARKER
}

class TraitImpl : Trait {
    override $DEFINITION_MARKER
}

class TestDelegate : Trait by TraitImpl() {
    fun test() {
        $TEST_BLOCK_MARKER
    }
}

fun box(): String {
    TestDelegate().test()

    return "OK"
}""", commonCasesWithTestNotRenamed)
// -------------------------

        suite("enum",
"""
enum class Foo {
    BAR
    $DEFINITION_MARKER

    fun test() {
        $TEST_BLOCK_MARKER
    }
}

fun box(): String {
    Foo.BAR.test()

    return "OK"
}""", commonCasesWithTestNotRenamed)
// -------------------------

        suite("enum",
"""
enum class Foo {
    $KEYWORD_MARKER
}

fun box(): String {
    ${testNotRenamed("Foo.$KEYWORD_MARKER")}

    return "OK"
}"""
        ) {
            case("entry", "", "", "")
        }
// -------------------------
    }
}

// DSL

class Case(val name: String, val testDeclaration: String, val testDeclarationInit: String,  val testBlock: String)
class Suite(val name: String, val code: String, val cases: List<Case>)

class CaseBuilder {
    val cases = arrayListOf<Case>()

    fun case(name: String, testDeclaration: String, testDeclarationInit: String, testBlock: String) {
        cases.add(Case(name, testDeclaration, testDeclarationInit, testBlock))
    }
}

class TestDataBuilder() {
    val suites = arrayListOf<Suite>()

    fun suite(name: String, code: String, f: CaseBuilder.() -> Unit) {
        val builder = CaseBuilder()
        builder.f()

        suites.add(Suite(name, PREAMBLE + code, builder.cases))
    }

    fun generate(testDataDirPath: String) {
        val testDataDir = File(testDataDirPath)
        if (!testDataDir.exists() && !testDataDir.mkdirs()) {
            println("Unable to find or create test data dir: '$testDataDirPath'.")
            return
        }

        for (suite in suites) {
            for (case in suite.cases) {

                // Uses small portions of keywords instead of ALL_KEYWORDS to avoid a combinatorial explosion
                // Each portion contains at least one keyword which should be escaped and at least one which should not.
                for (keyword in nextKeywordPortion()) {
                    val keywordWithEscapeIfNeed = if (keyword in SHOULD_BE_ESCAPED) "`$keyword`" else keyword

                    val out = suite.code
                            .replace(DEFINITION_MARKER, case.testDeclaration + case.testDeclarationInit)
                            .replace(DECLARATION_MARKER, case.testDeclaration)
                            .replace(DECLARATION_INIT_MARKER, case.testDeclarationInit)
                            .replace(TEST_BLOCK_MARKER, case.testBlock)
                            .replace("\"$KEYWORD_MARKER\"", "\"$keyword\"")
                            .replace(KEYWORD_MARKER, keywordWithEscapeIfNeed)


                    val fileName = "${suite.name.decapitalize()}${case.name.capitalize()}${keyword.capitalize()}.kt"

                    File(testDataDirPath + "/" + fileName).writeText(out)
                }
            }
        }
    }
}

fun generate(testDataDirPath: String, f: TestDataBuilder.() -> Unit) {
    val builder = TestDataBuilder()
    builder.f()
    builder.generate(testDataDirPath)
}

val DEFINITION_MARKER = "DEFINITION"
val DECLARATION_MARKER = "DECLARATION"
val DECLARATION_INIT_MARKER = "DECLARATION_INIT"
val TEST_BLOCK_MARKER = "TEST_BLOCK"
val KEYWORD_MARKER = "KEYWORD"

val PREAMBLE = """package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!
"""

fun testRenamed(reference: String = "$KEYWORD_MARKER") = "testRenamed(\"$KEYWORD_MARKER\", { $reference })"
fun testNotRenamed(reference: String = "$KEYWORD_MARKER") = "testNotRenamed(\"$KEYWORD_MARKER\", { $reference })"

val testRenamedByName = testRenamed()
val testRenamedByRef = testRenamed("::$KEYWORD_MARKER")

val testNotRenamedByName = testNotRenamed()
val testNotRenamedByRef = testNotRenamed("::$KEYWORD_MARKER")

// KEYWORDS

val ALL_KEYWORDS = JsFunctionScope.RESERVED_WORDS.toList()

val SHOULD_BE_ESCAPED = (ALL_KEYWORDS.filter { it in KeywordStringsGenerated.KEYWORDS } union setOf("catch", "finally")).toSortedList()
val SHOULD_NOT_BE_ESCAPED = ALL_KEYWORDS.filter { it !in SHOULD_BE_ESCAPED }.toSortedList()


// all keywords by portions

// cyclic keyword streams
val s1 = SHOULD_BE_ESCAPED.cyclicStream()
val s2 = SHOULD_NOT_BE_ESCAPED.cyclicStream()

val PORTION_PART_SIZE = 2;

fun nextKeywordPortion() = s1.take(PORTION_PART_SIZE).toList() + s2.take(PORTION_PART_SIZE).toList()

// CyclicStream

fun <T> List<T>.cyclicStream() = CyclicStream(this)

class CyclicStream<T>(val c: List<T>) : Stream<T> {
    var i = 0
    override fun iterator(): Iterator<T> = object : Iterator<T>{
        override fun next(): T {
            i = if (i >= c.size) 0 else i
            return c[i++]
        }
        override fun hasNext(): Boolean = true
    }
}