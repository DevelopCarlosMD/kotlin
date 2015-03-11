package secondaryConstructorHeader

fun main(args: Array<String>) {
    A(1, 2)
}

class A {
    val prop: Int = 3
    //Breakpoint!
    constructor(x: Int, y: Int, z: Int = 4) {}
}

// EXPRESSION: x + y + z
// RESULT: 7: I
