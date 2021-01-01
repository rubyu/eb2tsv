
package com.github.rubyu.ebquery

import org.kohsuke.args4j.{Option => Opt, Argument => Arg}

class CliOption {
  @Opt(
    name = "-d",
    metaVar = "dir",
    usage = "path to the directory containing Epwing's CATALOGS file")
  var dir: String = null

  @Opt(
    name = "-f",
    metaVar = "format",
    usage = "output format")
  var format: String = null

  @Opt(
    name = "-m",
    metaVar = "modules",
    usage = "modules used for rendering the output")
  var modules: String = null

  @Opt(
    name = "--ebmap",
    metaVar = "file",
    usage = "path to the EBWin's GAIJI mapping file")
  var ebMap: String = null

  @Arg(
    index = 0,
    metaVar = "words",
    usage = "query words")
  var words: Array[String] = null

  @Opt(
    name = "-h",
    aliases = Array("--help"),
    usage = "print help")
  var help: Boolean = false
}