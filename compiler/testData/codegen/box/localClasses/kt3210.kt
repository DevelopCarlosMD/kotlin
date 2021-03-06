package org.example

trait SomeTrait {}

trait KotlinProcessor<T> {
    fun execute(callback: KotlinCallback<T>?);
}

trait KotlinCallback<T> {
    fun on(t : T);
}

public class Test(name : String) : KotlinProcessor<SomeTrait> {
    public override fun execute(callback: KotlinCallback<SomeTrait>?) {
        if(callback != null) {
            class InlineTrait : SomeTrait {}

            var inlineTrait = InlineTrait()
            callback.on(inlineTrait)
        }
    }
}

fun box() : String {
    var f = "fail"
    Test("OK").execute(object : KotlinCallback<SomeTrait> {
        override fun on(t: SomeTrait) {
            f = "OK"
        }
    })
    return f
}