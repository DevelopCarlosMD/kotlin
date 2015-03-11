class B {
    public static void foo() {
        new A()
    }
}

class C extends A {
    public C() {
        super();
    }
}

class D extends A {
    public C() {
    }
}