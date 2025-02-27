package ru.spbstu.insys.libsl.parser

import java.nio.file.Path

val primitiveSemanticTypes = listOf("String", "Int", "Char", "Boolean")

fun LibraryDecl.getArrayTypesFromFunctionDecls(): Collection<ComplexSemanticType> =
    this.functions.flatMap { it.args }.map { it.type }.filterArrayTypes().toSet()

fun LibraryDecl.readIncludes(includesDir: Path): List<Path> =
    includes.map { includesDir.resolve("$it.lsl") }

fun Collection<SemanticType>.filterArrayTypes() = filterIsInstance<ComplexSemanticType>().filter { it.isArray() }

fun SemanticType.isArray() = this is ComplexSemanticType && enclosingType.typeName == "[]"

fun SemanticType.isPointer() = this is ComplexSemanticType && enclosingType.typeName == "*"

fun SemanticType.isReference() = typeName !in primitiveSemanticTypes

//fun Type.arrayType() = "$typeName[]"

operator fun List<TypeDecl>.get(semanticType: SemanticType) = first { it.semanticType == semanticType }

fun LibraryDecl.associateAutomataWithFunctions(): LibraryDecl {
    return copy(automata = automata.map { automaton ->
        automaton.copy(associatedFunctions = this.functions.filter { it.entity.type == automaton.name })
    })
}

fun LibraryDecl.addArrayTypeDecls(convertToCodeArrayType: (CodeType) -> CodeType): LibraryDecl {
    val arrayTypes = this.getArrayTypesFromFunctionDecls()
    val typeDecls = arrayTypes.map { arrayType ->
        val itemType = arrayType.innerType
        val codeItemType = types[itemType].codeType
        val codeArrayType = convertToCodeArrayType(codeItemType)
        TypeDecl(semanticType = arrayType, codeType = codeArrayType)
    }
    return this.copy(types = this.types + typeDecls)
}

private fun toCodeType(
    complexType: ComplexSemanticType,
    typeDeclarations: List<TypeDecl>,
    complexTypeConversion: Map<String, String>
): String {
    val rule = checkNotNull(complexTypeConversion[complexType.enclosingType.typeName])
    val innerTypeText = if (complexType.innerType is ComplexSemanticType) {
        toCodeType(complexType.innerType, typeDeclarations, complexTypeConversion)
    } else {
        typeDeclarations[complexType.innerType].codeType.typeName
    }
    return rule.format(innerTypeText)
}

fun LibraryDecl.addComplexTypesDecls(complexTypeConversion: Map<String, String>): LibraryDecl {
    val usedTypes = this.functions.flatMap { it.args }.map { it.type } + this.functions.map { it.returnValue }
    val complexTypes = usedTypes.filterIsInstance<ComplexSemanticType>()
    val typeDecls = complexTypes.map { complexType ->
        TypeDecl(
            semanticType = complexType,
            codeType = CodeType(toCodeType(complexType, this.types, complexTypeConversion))
        )
    }
    val automata = typeDecls.map { type ->
        Automaton(
            javaPackage = null,
            name = type.semanticType,
            states = listOf(),
            shifts = listOf(),
            extendable = false,
            statements = listOf()
        )
    }
    return this.copy(types = this.types + typeDecls, automata = this.automata + automata)
}

fun LibraryDecl.generateHandlersForArrayAndPointerTypes(): LibraryDecl {
    val arrayTypes = this.types.filter { it.semanticType.isArray() || it.semanticType.isPointer() }.toSet()
//    val automata = mutableListOf<Automaton>()
    val newFunctionDecl = mutableListOf<FunctionDecl>()
    for (arrayType in arrayTypes) {
        val itemType = (arrayType.semanticType as ComplexSemanticType).innerType // getItemType(type, types) TODO: Dirty
        val codeType = this.types[itemType].codeType.typeName //getArrayType(itemType)
        val baseFunctionDecl = FunctionDecl(
            entity = FunctionEntityDecl(
                arrayType.semanticType,
                FunctionEntityDecl.FunctionEntityDeclStyle.EXPLICIT_BEFORE_NAME
            ),
            name = "", args = listOf(),
            actions = listOf(), returnValue = null,
            staticName = null, properties = listOf(), builtin = true,
            variableAssignments = listOf(),
            contracts = ContractsInfo.empty
        )
        val set = baseFunctionDecl.copy(name = "set<$codeType>")
        val get = baseFunctionDecl.copy(name = "get<$codeType>")
        val memAlloc = baseFunctionDecl.copy(name = "mem_alloc<$codeType>")
        newFunctionDecl += listOf(set, get, memAlloc)
//        val arrayAutomaton = Automaton(name = type,
//                states = listOf(StateDecl("Created"), StateDecl("Constructed")),
//                shifts = listOf(), extendable = false)
//        automata += arrayAutomaton
    }
    return copy(functions = functions + newFunctionDecl)
}

val defaultStates = listOf(
    StateDecl("Created", isFinish = false),
    StateDecl("Constructed", isFinish = false),
    StateDecl("Closed", isFinish = true)
)

fun LibraryDecl.addDefaultStates(): LibraryDecl =
    copy(automata = automata.map { it.copy(states = (it.states + defaultStates).distinct()) })

fun LibraryDecl.addMissingAutomata(): LibraryDecl {
    val existingAutomataNames = automata.map { it.name }
    val generatedAutomata = functions
        .filter { it.entity.type !in existingAutomataNames }
        .map { Automaton(
            javaPackage = null,
            name = it.entity.type,
            states = defaultStates,
            shifts = listOf(),
            extendable = false,
            statements = listOf()
        ) }
    return this.copy(automata = automata + generatedAutomata)
}
