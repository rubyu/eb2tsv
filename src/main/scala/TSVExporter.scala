package com.github.rubyu.ebquery

import java.io.Writer
import com.github.tototoshi.csv._

class TSVExporter(w: Writer) extends IExporter {
  implicit val format = new TSVFormat {}
  val writer = CSVWriter.open(w)(format)

  private def escape(s: String): String =
    s.replaceAll(raw"\\", raw"\\\\").replaceAll(raw"\n", raw"\\n")

  override def export(indexValue: String, heading: String, description: String): Unit =
    writer.writeRow(List(escape(indexValue), escape(heading), escape(description)))

  def close(): Unit = writer.close()
}
