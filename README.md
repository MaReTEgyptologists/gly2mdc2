# Gly2Mdc v.2.0


Version 2.0 of the Gly2Mdc tool converts a .gly file produced with JSesh to 3 different texts:
                
1. cleaned Manuel de Codage encoding

2. stripped Manuel de Codage encoding in Ramses Transliteration and TLA corpora style

3. unicode characters.

Additionally makes a JSON-format text where the signs have been annotated with encoding, Unicode, and Thot Sign List designations


### Gly2mdc2-2.0.jar can be started by double-clicking.

Select file to open.
Different tabs show the different versions of the text.
Select which version or versions to save on file. The name of the file loaded is used, unless changed in the dialog box. The name format is <name>.json for the JSON version and <name>_mdc|pureMdc|unicode.txt for the other ones.

## **NOTES on the tool:**

Java 11 version has only been tested on a Mac, Java 8 (the jar file is in the Java8 folder) version also in a virtual Windows 10. 

Requires Java JRE installed on the computer

Build with Java JDK 11, but should work with all JRE versions above 1.9. The Java 8 version only works in JRE 1.8.

Some control characters might be shown as question marks in the tool but should work in the files created (depending on your OS). The tool uses Aegyptus font, but for reading the unicode txt-file, I recommend the newGardiner (https://mjn.host.cs.st-andrews.ac.uk/egyptian/fonts/newgardiner.html).

Large files with long texts take time to process and make the tool unusable while this happens. After Manuel de Codage has loaded, it may still take time to process the other formats.

## **NOTES on the choices made for converting the encoding:**

Gly2Mdc does NOT support the absolute placing of the signs when converted to Unicode (and JSON). For converting files with manually placed signs to accurate Unicode, HieroJax (https://nederhof.github.io/hierojax/mdcconversion.html) can be used. In Gly2Mdc the manually placed signs are simply presented one after another. Other placements are represented by Unicode format control characters, but the precision cannot always be guaranteed.

Since parentheses indicate croup in JSesh, the white spaces inside them have been changed to * (i.e. signs next to each other).

In Unicode, shading control characters targets only one character; therefore, the shading marked in JSesh has been converted to target each of the signs in a group. For example, p:n#12 (upper part of group shaded) becomes p#1234:n. All characters between #b - #e get shaded with #1234. For groups with more than 2 levels (e.g. p:n:n:n), the shading may be slightly off.

All the different ways to indicate ligatures after the sign in JSesh (&&& && & _& **) are changed to &. A list (src/resources/myIns.txt) made on the bases of https://nederhof.github.io/hierojax/insertionlist.html is used for determining which side of a sign another sign should go to in a ligature. Gly2Mdc uses the ligatures that are specified in the ligatures.txt file in the JSesh source code + D&d.

## **JSON format contains the following**

- textName = 1. line shown in JSesh
- infos = all the other lines before the actual hieroglyphic text starts
- items	= signs or comment lines

Each sign has (when applicable):
- line		= line number
- itemNr	= sign position on the line
- originalLine = original line number, whether in the beginning of line or inside text
- encoding	= encoding as typed in JSesh
- mdc		= Gardiner sign list designation of the sign
- unicode	= Unicode character of the sign if available, mdc otherwise
- codepoint	= Unicode character's codepoint
- tsl		= link to the sign in Thot Sign List (https://thotsignlist.org)
- shading	= YES if the sign is followed by shading that applies to it, NO otherwise
- controlCharacter	= YES if the sign is a Unicode control character
- rotation	= if the sign has an attribute for rotating it, e.g. \R270
- size		= if the sign has an attribute that defines its size, e.g. \85


## **TO DO:**

INFO button

Updated version loaded in January 2025:

~~Test on a Windows computer~~

~~Add red colour support~~

