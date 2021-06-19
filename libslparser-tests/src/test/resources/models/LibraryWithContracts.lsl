library LibraryWithFinishStates;

types {
    CustomString (String);
    Int (Int);
}

automaton Test {
    state A;
}

fun Test.foo(arg1: Int) {
    requires (a > 1) || (a < 2) && (a >= 1) || !(a != 155.2) || (a == "foo(\"123\")");
    ensures (a > 1) || (a < 2) && (a >= 1) || (result=old(arg1)*2);
}