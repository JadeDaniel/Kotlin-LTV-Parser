package co.couldbe.demo.tags


enum class EMVTags(val definition: TagDefinition) {
    FCITemplate(TagDefinition(0x6F.toUByte(), isConstructed = true)),
    DF(TagDefinition( 0x84.toUByte(), FCITemplate.definition)),
    FCIProprietaryTemplate(TagDefinition(0xA5.toUByte(), isConstructed = true)),
    ShortFileIdentifier(TagDefinition(0x88.toUByte())),
    LanguagePreference(TagDefinition(0x5F2D.toUByte())),
    ; // end enum entries

    companion object {
        const val MAX_TAG_LENGTH = 2

        fun fromByte(tag: UByte): TagDefinition? =
            entries.find { it.definition.tag == tag }?.definition
    }
}
