
package com.github.rubyu.ebquery

import collection.mutable.ListBuffer
import collection.mutable.Map


trait EBElem {
  var parent: EBElem = null
  val children = new ListBuffer[Any]
  def _childrenString = children map (_.toString) mkString
  override def equals(other: Any) = other.toString == this.toString
}


trait EBTagElem {
  val tag: String
  val property = Map.empty[String, String]
  def _openerString = Util.escape(tag) + _propertyString
  def _closerString = Util.escape(tag)
  //todo support empty value
  def _propertyString = property.toSeq.sorted map (t =>
    " %s=\"%s\"".format(Util.escape(t._1), Util.escape(t._2))) mkString
}


class EBLeaf(val tag: String) extends EBElem with EBTagElem {
  override val children = null
  override def toString = "<%s>".format(_openerString)
}


class EBNode(val tag: String) extends EBElem with EBTagElem {
  override def toString = "<%s>%s</%s>".format(_openerString, _childrenString, _closerString)
}


class EBTransparentNode extends EBElem {
  override def toString = _childrenString
}


trait GraphicNode extends EBElem with EBTagElem {
  override def toString = {
    if (property.contains("src")) {
      "%s<%s>".format(_childrenString, _openerString)
    } else {
      _childrenString
    }
  }
}


class MonoGraphicNode(val width: Int, val height: Int) extends GraphicNode {
  val tag = "img"
}


class ColorGraphicNode(val format: Int, val pos: Long) extends GraphicNode {
  val tag = "img"
}


class AudioNode(val format: Int, val start: Long, val end: Long) extends EBElem with EBTagElem {
  val tag = "audio"
  override def toString = {
    if (property.contains("src")) {
      "%s<%s></%s>".format(_childrenString, _openerString, _closerString)
    } else {
      _childrenString
    }
  }
}


class EBElemTree {
  val root = new EBTransparentNode
  var current: EBElem = root

  def clear() {
    current = root
    root.children.clear()
  }

  def append(value: Any) {
    if (Option(current.children) isEmpty) throw new IllegalStateException
    current.children += value

  }

  def open(elem: EBElem) {
    append(elem)
    elem.parent = current
    current = elem
  }

  def close() {
    if (Option(current.parent) isEmpty) throw new IllegalStateException
    current = current.parent
  }
}
