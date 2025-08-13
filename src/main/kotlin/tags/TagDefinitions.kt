package co.couldbe.demo.tags


@OptIn(ExperimentalUnsignedTypes::class)
enum class EMVTags(val definition: TagDefinition) {
    FCITemplate(TagDefinition(0x6F, isConstructed = true)),
    DF(TagDefinition(0x84, FCITemplate.definition)),
    FCIProprietaryTemplate(TagDefinition(0xA5, isConstructed = true)),
    ShortFileIdentifier(TagDefinition(0x88)),
    LanguagePreference(TagDefinition(ubyteArrayOf(0x5F.toUByte(), 0x2D.toUByte()))),
    ; // end enum entries

    companion object {
        const val MAX_TAG_LENGTH = 2

        @OptIn(ExperimentalUnsignedTypes::class)
        fun fromBytes(tag: UByteArray): TagDefinition? =
            entries.find { it.definition.tag.contentEquals(tag) }?.definition
    }
}
