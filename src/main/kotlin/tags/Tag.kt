package co.couldbe.demo.tags

@OptIn(ExperimentalUnsignedTypes::class)
data class Tag(
    val definition: TagDefinition,
    val length: Int,
    val value: ByteArray
) {
    fun isConstructed() = definition.isConstructed
    fun template() = definition.template
    fun tag() = definition.tag
}