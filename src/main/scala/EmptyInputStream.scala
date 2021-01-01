package com.github.rubyu.ebquery

import java.io.InputStream

class EmptyInputStream extends InputStream {
  override def read() = -1
}
