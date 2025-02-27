grammar LibSL;

start
   :   libslHeaderSection sections EOF
   ;

libslHeaderSection
   :   'library' libraryName ';'
   ;

sections
   :   (importSection
          |   includeSection
          |   typesSection
          |   convertersSection
          |   automatonDescription
          |   funDecl)+
   ;

includeSection
   :   'includes' '{' includedStatement+ '}'
   ;

includedStatement
   :   includedName ';'
   ;


includedName
   :   Identifier
   ;

importSection
   :   'imports' '{' importedStatement+ '}'
   ;

importedStatement
   :   importedName ';'
   ;


importedName
   :   (Identifier|'.'|'/'|'\\')+
   ;

typesSection
   :   'types' '{' typeDecl+ '}'
   ;

typeDecl
   :   semanticType '(' codeType ')' ';'
   ;

semanticType
   :   semanticType '<' semanticType '>'
   |   semanticType arrayIdentifier
   |   semanticType pointerIdentifier
   |   pureSemanticType
   ;

arrayIdentifier
   :
   '[]'
   ;

pointerIdentifier
   :
   '*'
   ;

pureSemanticType
   : Identifier
   ;

codeType
   :   codeType '<' codeType '>'
   |   codeType arrayIdentifier
   |   codeType pointerIdentifier
   |   typeUnit
   ;

typeUnit
   :   prefixedSemanticType
   |   complexType
   ;

complexType
   :   Identifier ('.' complexType)?
   ;

prefixedSemanticType
   :   'sem:' pureSemanticType
   ;

convertersSection
   :   'converters' '{' converter+ '}'
   ;

converter
   :   destEntity '<-' converterExpression ';'
   ;

destEntity
   :   semanticType
   ;

converterExpression
   :   (Identifier|'('|')'|'"'|'<'|'>'|'.'|','|'/'|'\\'|'-')*
   ;

automatonDescription
   :   'automaton' automatonName '{' javapackage? (stateDecl|finishstateDecl|shiftDecl|extendableFlag|automatonStatement)* '}'
   ;

javapackage
   :   'javapackage' Identifier('.'Identifier)* ';'
   ;

automatonStatement
   :   automatonVariableDecl
   ;

automatonVariableDecl
   :   'var' Identifier ':' semanticType ';'
   ;

stateDecl
   :   'state' stateName (',' stateName)* ';'
   ;

finishstateDecl
   :   'finishstate' stateName ';'
   ;

stateName
   :   Identifier
   ;

shiftDecl
   :   'shift' srcState '->' dstState '(' funName (',' funName)* ')' ';'
   ;

srcState
   :   Identifier
   ;

dstState
   :   Identifier
   ;

automatonName
   :   semanticType
   ;

extendableFlag
   :   'extendable;'
   ;

funDecl
   :   'fun' (entityName '.')? funName '(' funArgs? ')' (':' funReturnType)? (';' | '{' funRequires? funProperties* funEnsures?'}')
   ;

funRequires
   :   'requires' expression ';'
   ;

funEnsures
   :   'ensures' expression ';'
   ;

expression
   :   conjunction
   ;

conjunction
   :   conjunctionTermWithInversion ('&&' conjunctionTermWithInversion)*
   ;

conjunctionTermWithInversion
   :   inversion? '(' disjunction ')'
   ;

disjunction
   :   term ('||' term)*
   ;

inversion
   :   '!'
   ;

term
   :   inversion? ( '(' ) (variableName | functionCall | equality) ( ')' )
   |   inversion? (variableName | functionCall | equality)
   ;

functionCall
   :   Identifier '(' functionArgs? ')'
   ;

functionArgs
   :   functionArg (',' functionArg)*
   ;

functionArg
   :   equalityPart
   ;

variableName
   :   Identifier
   ;

equality
   :   equalityPart compareOp equalityPart
   ;

equalityPart
   :   arithmeticExpression
   |   String
   |   conjunction
   ;

compareOp
   :   '==' | '!=' | '>=' | '<=' | '>' | '<'
   ;

arithmeticExpression
   :   arithmeticExpression arithmeticSignMulDiv arithmeticExpression
   |   arithmeticExpression arithmeticSignAddSub arithmeticExpression
   |   Number
   |   functionCall
   |   variableName
   ;

arithmeticSignMulDiv
   :   '*'
   |   '/'
   ;

arithmeticSignAddSub
   :   '+'
   |   '-'
   ;

Number
   :   ('0' .. '9')+ ('.' ('0' .. '9')+ )? // todo: add scientific notation
   ;

String
   :   '"' ('\\"' | .)*? '"'
   ;

funProperties
   :   actionDecl
   |   'when' ';'
   |   propertyDecl
   |   variableAssignment
   |   staticDecl
   ;

actionDecl
   :   'action' actionName '(' (','? Identifier)* ')' ';'
   ;

propertyDecl
   :   'property' '"' propertyKey '"' '=' '"' propertyValue '"' ';'
   ;

propertyKey
   :   Identifier
   ;

propertyValue
   :   Identifier
   ;

staticDecl
   :   'static' ('"' staticName '"')? ';'
   ;

staticName
   :   Identifier
   ;

actionName
   :   Identifier
   ;

entityName
   :   semanticType
   ;

funName
   :   Identifier
   ;

variableAssignment
   :   Identifier '=' 'new' automatonName '(' automatonArgs? ')' ';'
   ;

automatonArgs
   :   automatonArg
   ;

automatonArg
   :   Identifier
   ;

funArgs
   :   funArg (',' funArg)*
   ;

funArg
   :   annotation* argName ':' argType
   ;

annotation
   :   '@' annotationName
   ;

annotationName
   :   Identifier;

argName
   :   Identifier
   ;

argType
   :   semanticType
   ;

funReturnType
   :   annotation* semanticType
   ;

containsTreeType
   :   Identifier;

isTreeType
   :   Identifier;

variableType
   :   Identifier;

libraryName
   :   Identifier;

Identifier
   :   JavaLetter JavaLetterOrDigit*
   ;

//numberDesc
//    : orderType? DecNumbers
//    ;
//
//orderType
//    : ('>'|'<'|'<='|'>=')
//    ;
//
//DecNumbers
//    :   ('0'..'9')+
//    ;

fragment
JavaLetter
   :   [a-zA-Z$_] // these are the "java letters" below 0xFF
   |   // covers all characters above 0xFF which are not a surrogate
       ~[\u0000-\u00FF\uD800-\uDBFF]
       {Character.isJavaIdentifierStart(_input.LA(-1))}?
   |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
       [\uD800-\uDBFF] [\uDC00-\uDFFF]
       {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
   ;

fragment
JavaLetterOrDigit
   :   [a-zA-Z0-9$_] // these are the "java letters or digits" below 0xFF
   |   // covers all characters above 0xFF which are not a surrogate
       ~[\u0000-\u00FF\uD800-\uDBFF]
       {Character.isJavaIdentifierPart(_input.LA(-1))}?
   |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
       [\uD800-\uDBFF] [\uDC00-\uDFFF]
       {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
   ;

fragment
NEWLINE
  : '\r' '\n' | '\n' | '\r'
  ;

//
// Whitespace and comments
//

WS
   :   [ \t]+ -> skip
   ;

BR
   :   [\r\n\u000C]+ -> skip
   ;

COMMENT
   :   '/*' .*? '*/' -> skip
   ;

LINE_COMMENT
   :   '//' ~[\r\n]* -> skip
   ;

