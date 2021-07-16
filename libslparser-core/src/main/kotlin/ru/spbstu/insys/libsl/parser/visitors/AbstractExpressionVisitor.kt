package ru.spbstu.insys.libsl.parser.visitors

import ru.spbstu.insys.libsl.parser.*

abstract class AbstractExpressionVisitor<T> {
     open fun visit(node: ExpressionNode) : T {
        return when (node) {
            is ConjunctionNode -> visitConjunctionNode(node)
            is DisjunctionNode -> visitDisjunctionNode(node)
            is EqualityNode -> visitEqualityNode(node)
            is DivNode -> visitDivNode(node)
            is MinusNode -> visitMinusNode(node)
            is MulNode -> visitMulNode(node)
            is PlusNode -> visitPlusNode(node)
            is FunctionCallNode -> visitFunctionCallNode(node)
            is NumberNode -> visitNumberNode(node)
            is VariableNode -> visitVariableNode(node)
            is StringNode -> visitStringNode(node)
            is BooleanExpression -> visitConjunctionNode(node.expr)
        }
    }

    abstract fun visitConjunctionNode(node: ConjunctionNode): T

    abstract fun visitDisjunctionNode(node: DisjunctionNode): T

    abstract fun visitEqualityNode(node: EqualityNode): T

    abstract fun visitDivNode(node: DivNode): T

    abstract fun visitMinusNode(node: MinusNode): T

    abstract fun visitMulNode(node: MulNode): T

    abstract fun visitPlusNode(node: PlusNode): T

    abstract fun visitFunctionCallNode(node: FunctionCallNode): T

    abstract fun visitNumberNode(node: NumberNode): T

    abstract fun visitVariableNode(node: VariableNode): T

    abstract fun visitStringNode(node: StringNode): T
}