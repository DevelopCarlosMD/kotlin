package secondaryConstructorBody

fun main(args: Array<String>) {
    A(1, 2)
}

class A {
    val prop: Int
    constructor(x: Int, y: Int) {
        //Breakpoint!
        prop = 2 * x
    }
}

// EXPRESSION: prop + x + y
// RESULT: 4: I
