fun main(args: Array<String>) {
    A1()
    A3()
    A5()
    A7()
    A9()
    A11()
    A13()
}

class A1 {
    constructor() {}
}

open class A2
class A3 : A2 {
    constructor() /* implicit super call to A2 constructor */ {}
}

open class A4 {
    constructor() {}
}
class A5 : A4 {
    constructor() /* implicit super call to A4 constructor */ {}
}

open class A6 {
    constructor() {}
}
class A7 : A6()

open class A8(x: Int)
class A9 : A8 {
    constructor(): super(1) {}
}

open class A10 {
    constructor(x: Int) {}
}
class A11 : A10 {
    constructor(): super(1) {}
}

opne class A12 {
    constructor(x: Int) {}
}
class A13 : A12(2)
