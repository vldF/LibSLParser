library LibraryWithFinishStates;

types {
    CustomString (String);
}

automaton Test {
    state A;
}

fun Test.foo() {
    requires (a > 1) || (a < 2) && (a >= 1) || !(a != 155.2) || (a == "foo(\"123\")");
}