// SIBLING:
class MyClass {
    fun test(): Int {
        <selection>coFun()
        return coProp + 10</selection>
    }

    default object {
        val coProp = 1

        fun coFun() {

        }
    }
}
