library LibraryWithFinishStates;

types {
    CustomString (String);
    Int (Int);
}

automaton Test {
    state A;
}

fun Test.foo(a: Int) {
    requires (a > 1) && (a == "test(\"123\")") && !(a != 155.2) && (a == foo(123));
    ensures (a > 1 || b <= 4) && (a < 2)  && !(a != 155.2) && (a == foo(123));
}

fun Test.bar(a: Int) {
    requires (a > 1 + 2 * 3) && (2 / 3 > a);
}