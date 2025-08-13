package co.couldbe.demo.tags

data class Tag(
    val definition: TagDefinition,
    val length: Int,
    val value: ByteArray
) {
    fun isConstructed() = definition.isConstructed
}