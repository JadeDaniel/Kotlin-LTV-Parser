package co.couldbe.demo.tags

import co.couldbe.demo.prettyTagNameHex

data class TagDefinition @OptIn(ExperimentalUnsignedTypes::class) constructor(
    val tag: UByteArray,
    /* template is a pointer to a tag that this tag is allowed to appear */
    val template: TagDefinition? = null,
    val isConstructed: Boolean = false
) {
    /**
     * Secondary constructor that creates a TagDefinition from an integer value
     * @param tagValue Integer value to be converted to UByteArray
     * @param template Optional template tag definition
     * @param isConstructed Boolean indicating if the tag is constructed
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    constructor(
        tagValue: Int,
        template: TagDefinition? = null,
        isConstructed: Boolean = false
    ) : this(ubyteArrayOf(tagValue.toUByte()), template, isConstructed)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun toString(): String {
        val attributeString = listOfNotNull(
            "tag=${tag.prettyTagNameHex()}",
            "type=${if (isConstructed) "Constructed" else "Primitive"}",
            template?.let { "template=${template.tag.prettyTagNameHex()}" },
        ).joinToString()

        return "Tag($attributeString)"
    }
}