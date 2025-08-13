package co.couldbe.demo.tags

data class TagDefinition (
    val tag: UByte,
    /* template is a pointer to a tag that this tag is allowed to appear */
    val template: TagDefinition? = null,
    val isConstructed: Boolean = false
) {
    override fun toString(): String {
        return "Tag(type=${if (isConstructed) "Constructed" else "Primitive"}, tag=${tag.toString(16).uppercase()}, template=$template)"
    }
}