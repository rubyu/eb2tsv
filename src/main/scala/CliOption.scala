
package com.github.rubyu.ebquery

import org.kohsuke.args4j.{Option => Opt, Argument => Arg}

class CliOption {
  @Opt(
    name = "-d",
    metaVar = "dir",
    usage = "path to the directory containing Epwing's CATALOGS file")
  var dir: String = null

  @Opt(
    name = "--ebmap",
    metaVar = "file",
    usage = "path to the EBWin's GAIJI mapping file")
  var ebMap: String = null

  @Opt(
    name = "-h",
    aliases = Array("--help"),
    usage = "print help")
  var help: Boolean = false
}