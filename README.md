# NAME

```
eb2tsv -- Dump Tool for EPWING(Electronic Publishing-WING) Dictionary
```

# SYNOPSIS

```
 java -jar eb2tsv.jar [-h | --help]
 java -jar eb2tsv.jar --ebmap map_file -d dictionary_dir
```

# DESCRIPTION
Dump all entries in a EPWING dictionary, and prints results to standard output.The options are the following:

```
 -d dir       : Path to the directory containing Epwing's CATALOGS file.
 --ebmap file : Path to the EBWin's GAIJI mapping file.
 -h (--help)  : Print help.
```


## External Character
**--ebmap** *file* option is must. This is required for converting an external character to an unicode character.
This tool uses [EBWin](http://www31.ocn.ne.jp/~h_ishida/EBPocket.html)/[EBWin4](http://ebstudio.info/manual/EBWin4/EBWin4.html) compatible map file. 
When installing the software, map files for an numerous number of dictionaries will also be installed to "%APPDATA%\Roaming\EBWin\GAIJI" or "%APPDATA%\Roaming\EBWin4\GAIJI".

Note: There are some differences in eb2tsv's intepretation on the external character replacement from EBWin's.
Firstly, **eb2tsv only uses the first and the second column** as the identifier and the replacement;
the third column, for compatibility with non-unicode system, will be ignored.
Secondly, **eb2tsv supports the replacement longer than 3 characters**, on the other hand
EBWin/EBWin4 only supports 3 characters or less.


### The details of the map file
[外字定義ファイル GAIJI/*.map](http://ebstudio.info/manual/EBPocket/0_0_4_4.html)

## Encoding
When using on Windows or other non-unicode platform,
you need to change the "file.encoding" property to "utf-8" as follows:

```
> java -Dfile.encoding=utf-8 -jar eb2tsv.jar ...
```

Otherwise, all characters that the default file encoding doesn't support will be converted to "?".


## 1.0.0
Initial release.
