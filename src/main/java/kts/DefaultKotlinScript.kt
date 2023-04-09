package kts

open class DefaultKotlinScript {

    inline fun plugins(arg: String?) {
        println("plugins block")
        println(arg)
    }
}