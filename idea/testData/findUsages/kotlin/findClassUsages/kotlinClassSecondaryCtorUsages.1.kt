fun foo() {
    A()
}

class B : A()
class C : A {
    constructor() {}
}
class D : A {
    constructor(): super() {}
}