# Welcome to the Kotlin LTV Parser.

Length Tag Value parsing is a simple and effective way to send binary data over-the-wire. It prevents unnecessary look-ahead or speculative reading, and allows the parser to know exactly what it's looking for.

I will be using EMV TLV as the basis for this project. I did some research and discovered that TLV is commonly used in the payments space, and EMV is a particular example of that.

## Usage
### Running the Application

You can use the gradle wrapper to build and run the application from the command line.
```
   ./gradlew build run
```

You should see the output from the decoded sample TLV data.

```aiignore
FCITemplate (6F) 
  DF (84)  value [31 50 41 59 2E 53 59 53 2E 44 44 46 30 31]
  FCIProprietaryTemplate (A5) 
    ShortFileIdentifier (88)  value [02]
    LanguagePreference (5F 2D)  value [65 6E]
```

### Running the test suite
```aiignore
   ./gradlew test
```

### Tooling Versions
```
- IntelliJ IDEA 2025.2 (Ultimate Edition)
- Kotlin plugin 2.2.0
- Gradle 8.14
- Java 24 (OpenJDK)
- using BinEd plugin in IntelliJ to edit and view sample TLV
```

## Assumptions:
- Tags must appear either at the top level, or on one template tag. A tag cannot be allowed to appear on multiple template tags.
- The Length byte is a simple short value and is always only 1 byte long. I understand that in the real world there is variability here and a more complex way of reading length. 
- Tags are allowed to be empty.
- EMV-TLV tags can be either primitives or constructed. 
  - Constructed tags have nested tags and no simple data on them.
  - Primitive tags contain a byte array value and no nested tags.

## Sample EMV-TLV data
This data is used in the main function to generate the sample output.

`6F 1A 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 08 88 01 02 5F 2D 02 65 6E`

You can view it [decoded and nicely broken down](https://emvlab.org/tlvutils/?data=6F1A840E315041592E5359532E4444463031A5088801025F2D02656E) here.

## Future Improvements
- it would be nice to have an extensive test Domain Specific Language (DSL) to allow defining complex nested test data easily.


### Additional Reading:
- TLV
  - https://dev.to/dariocasciato/tlv-a-powerful-tool-for-handling-data-in-embedded-systems-f4e
  - Wikipedia Entry on TLV https://en.wikipedia.org/wiki/Type–length–value
  - https://stackoverflow.com/questions/4413080/what-is-tlv-tag-length-value
  - 
- EMV-TLV
  - https://mvallim.github.io/emv-qrcode/docs/EMV_v4.3_Book_4_Other_Interfaces_20120607062305603.pdf
  - https://medium.com/@lovisgod/understanding-emv-introduction-to-tlv-40d66bd004e7
  - https://skryvets.com/blog/2020/05/02/what-are-emv-tags/

## Working Questions
- Does Apple Pay use TLV? If so, big endian or little endian data?
- Would a CLI interface be useful to demonstrate the use of this code?