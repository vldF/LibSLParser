package ru.spbstu.insys.libsl.parser.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import ru.spbstu.insys.libsl.parser.*
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParserTest {
    private fun readResourceAsString(name: String) = this.javaClass.classLoader.getResourceAsStream(name)

    private fun makeDiff(expected: String, actual: String) = expected.lineSequence().zip(actual.lineSequence())
        .filter { (source, result) -> source != result }
        .map { (source, result) -> "-$source\n+$result" }
        .joinToString("\n")

    @Test
    fun z3test() {
        val sourceModel = assertNotNull(readResourceAsString("models/Z3.lsl"))
        val parsedModel = ModelParser().parse(sourceModel)
        val text = parsedModel.print()
        assertEquals("library Z3;", text.lineSequence().first())
        assertTrue(parsedModel.functions.any { it.name == "Z3_mk_bool_sort" })
        val expected = assertNotNull(readResourceAsString("models/Z3.lsl"))
            .bufferedReader()
            .use { it.readText() }
        assertEquals(expected, text, message = makeDiff(expected, text))
    }

    @ParameterizedTest
    @ValueSource(strings = ["SocketServer", "Requests", "OkHttp", "HttpURLConnection"])
    fun modelParseTest(modelName: String) {
        val sourceModel = assertNotNull(readResourceAsString("models/$modelName.lsl"))
        val parsedModel = ModelParser().parse(sourceModel)
        assertEquals(modelName, parsedModel.name)
    }

    @Test
    fun parseFromStringTest() {
        val model = "library test; types {A (B);} automaton Test {}"
        val parsedModel = ModelParser().parse(model)
        assertEquals("test", parsedModel.name)
        assertEquals(1, parsedModel.types.size)
        assertEquals("A", parsedModel.types.single().semanticType.typeName)
        assertEquals("B", parsedModel.types.single().codeType.typeName)
        assertEquals(1, parsedModel.automata.size)
        assertEquals("Test", parsedModel.automata.single().name.typeName)
    }

    @Test
    fun parseFromReaderTest() {
        val model = "library test; types {A (B);} automaton Test {}"
        val reader = StringReader(model)
        val parsedModel = ModelParser().parse(reader)
        assertEquals("test", parsedModel.name)
        assertEquals(1, parsedModel.types.size)
        assertEquals(1, parsedModel.automata.size)
    }

    @Test
    fun parseWithPackage() {
        val model = "library test; types{A (B);} automaton Test {javapackage ru.spbstu.test.package;}"
        val reader = StringReader(model)
        val parsedModel = ModelParser().parse(reader)
        assertEquals("ru.spbstu.test.package", parsedModel.automata[0].javaPackage?.name)
    }

    @Test
    fun parseWithVariables() {
        val sourceModel = assertNotNull(readResourceAsString("models/LibraryWithVariables.lsl"))
        val parsedModel = ModelParser().parse(sourceModel)
        val variable = parsedModel.automata[0].statements[0] as AutomatonVariableStatement
        assertEquals(variable.name, "testVariable")
        assertEquals(variable.type, "CustomString")

        val assignment = parsedModel.functions[0].variableAssignments[0]
        assertEquals(assignment.name, "testVariable")
        assertEquals(assignment.calleeAutomatonName, "Test")
        assertEquals(assignment.calleeArguments, listOf("A"))
    }

    @Test
    fun parseWithFinalStates() {
        val sourceModel = assertNotNull(readResourceAsString("models/LibraryWithFinishState.lsl"))
        val parsedModel = ModelParser().parse(sourceModel)
        val allStates = parsedModel.automata[0].states
        val nonFinishStates = allStates.filter { !it.isFinish }
        val finishStates = allStates.filter { it.isFinish }

        assertEquals(2, nonFinishStates.size)
        assertEquals(1, finishStates.size)

        assertEquals("F", finishStates.first().name)
    }

    @Test
    fun parseWithContracts() {
        val sourceModel = assertNotNull(readResourceAsString("models/LibraryWithContracts.lsl"))
        val parsedModel = ModelParser().parse(sourceModel)

        val func = parsedModel.functions.first()
        val requires = func.contracts.requires
        val ensures = func.contracts.ensures

        assertNotNull(requires)
        assertNotNull(ensures)

        assertEquals(4, requires.disjunctions.size)
        assertEquals("a", ((requires.disjunctions[0].TermList.first() as? EqualityNode)?.left as? VariableNode)?.name)
        assertEquals(155.2, ((requires.disjunctions[2].TermList.first() as? EqualityNode)?.right as? NumberNode)?.value)
        assertEquals("foo", ((requires.disjunctions[3].TermList.first() as? EqualityNode)?.right as? FunctionCallNode)?.name)
        assertEquals(123L, (((requires.disjunctions[3].TermList.first() as? EqualityNode)?.right as? FunctionCallNode)?.args?.first() as? NumberNode)?.value)

        assertEquals(4, ensures.disjunctions.size)
    }
}