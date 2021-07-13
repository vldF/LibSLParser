package ru.spbstu.insys.libsl.parser

sealed class ExpressionNode : Node

class ConjunctionNode(val disjunctions: List<DisjunctionNode>) : ExpressionNode()

class DisjunctionNode(val TermList: List<TermNode>, var isInverted: Boolean) : ExpressionNode()

sealed class TermNode : ExpressionNode() {
    abstract var isInverted: Boolean
}

class VariableNode(val name: String, override var isInverted: Boolean) : ArithmeticExpressionNode()

class FunctionCallNode(val name: String, val args: List<EqualityPartNode>, override var isInverted: Boolean) : ArithmeticExpressionNode()

class EqualityNode(
    val left: EqualityPartNode,
    val right: EqualityPartNode,
    val sign: EqualitySign,
    override var isInverted: Boolean
    ) : TermNode()

enum class EqualitySign(val text: String) {
    EQ_EQ("=="), NOT_EQ("!="), LT_EQ("<="), GT_EQ(">="), LT("<"), GT(">")
}

sealed class EqualityPartNode : TermNode() {
    override var isInverted: Boolean = false
}

sealed class ArithmeticExpressionNode : EqualityPartNode()

sealed class UnaryArithmeticExpressionNode : ArithmeticExpressionNode()

sealed class BinaryArithmeticExpressionNode : ArithmeticExpressionNode()

class NumberNode(val value: Number) : UnaryArithmeticExpressionNode()

class StringNode(val value: String) : EqualityPartNode()

class MulNode(val left: ArithmeticExpressionNode, val right: ArithmeticExpressionNode) : BinaryArithmeticExpressionNode()

class DivNode(val left: ArithmeticExpressionNode, val right: ArithmeticExpressionNode) : BinaryArithmeticExpressionNode()

class MinusNode(val left: ArithmeticExpressionNode, val right: ArithmeticExpressionNode) : BinaryArithmeticExpressionNode()

class PlusNode(val left: ArithmeticExpressionNode, val right: ArithmeticExpressionNode) : BinaryArithmeticExpressionNode()

