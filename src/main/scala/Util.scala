
package com.github.rubyu.ebquery

import io.github.eb4j.{EBException, SubBook}
import io.github.eb4j.util.ImageUtil
import org.apache.commons.lang.ArrayUtils
import java.awt.Color
import EBProcessor.{ExternalCharacterType, Wide, Narrow}
import org.apache.commons.codec.binary.Base64.encodeBase64

object Util {

  def base64str(data: Array[Byte]) = new String(encodeBase64(data), "ASCII")

  def escape(text: String) = {
    val s = new StringBuilder
    val len = text.length
    var pos = 0
    while (pos < len) {
      text.charAt(pos) match {
        case '<' => s.append("&lt;")
        case '>' => s.append("&gt;")
        case '&' => s.append("&amp;")
        case '"' => s.append("&quot;")
        case '\'' => s.append("&#39;")
        case '\n' => s.append('\n')
        case '\r' => s.append('\r')
        case '\t' => s.append('\t')
        case c => if (c >= ' ') s.append(c)
      }
      pos += 1
    }
    s.toString
  }

  /*
   /**
     * 外字イメージを返します。
     *
     * @param subbook 副本
     * @param narrow 半角/全角フラグ
     * @param code 外字のコード
     * @param fore 前景色
     * @param back 背景色
     * @return PNGデータ
     */
    private byte[] _getFontImage(SubBook subbook, boolean narrow,
                                 int code, int fore, int back) {
        byte[] data = null;
        int width = 0;
        try {
            if (narrow) {
                if (!subbook.getFont().hasNarrowFont()) {
                    return new byte[0];
                }
                data = subbook.getFont().getNarrowFont(code);
                width = subbook.getFont().getNarrowFontWidth();
            } else {
                if (!subbook.getFont().hasWideFont()) {
                    return new byte[0];
                }
                data = subbook.getFont().getWideFont(code);
                width = subbook.getFont().getWideFontWidth();
            }
        } catch (EBException e) {
            _logger.warn("failed to load font image", e);
            return new byte[0];
        }
        if (ArrayUtils.isEmpty(data)) {
            return new byte[0];
        }
        int height = subbook.getFont().getFontHeight();
        return ImageUtil.bitmapToPNG(data, width, height,
                                     new Color(fore), new Color(back), true, 9);
    }
   */
  def externalCharacterImage(dic: SubBook, _type: ExternalCharacterType, code: Int): Option[Array[Byte]] = {
    val (data, w) = try {
      _type match {
        case Narrow => {
          if (!dic.getFont.hasNarrowFont) return None
          (dic.getFont.getNarrowFont(code), dic.getFont.getNarrowFontWidth)
        }
        case Wide => {
          if (!dic.getFont.hasWideFont) return None
          (dic.getFont.getWideFont(code), dic.getFont.getWideFontWidth)
        }
      }
    } catch {
      case _: EBException => return None
    }
    if (ArrayUtils.isEmpty(data)) return None
    val h = dic.getFont.getFontHeight
    Some(ImageUtil.bitmapToPNG(data, w, h, Color.BLACK, Color.WHITE, true, 9))
  }

  /*
   /**
     * モノクロ画像を返します。
     *
     * @param subbook 副本
     * @param pos 画像の位置
     * @param width 画像の幅
     * @param height 画像の高さ
     * @return PNGデータ
     */
    private byte[] _getMonoImage(SubBook subbook, long pos, int width, int height) {
        byte[] data = null;
        try {
            data = subbook.getGraphicData().getMonoGraphic(pos, width, height);
        } catch (EBException e) {
            _logger.warn("failed to load mono image", e);
            return new byte[0];
        }
        if (ArrayUtils.isEmpty(data)) {
            return new byte[0];
        }
        return ImageUtil.bitmapToPNG(data, width, height,
                                     Color.BLACK, Color.WHITE, false, 9);
    }
   */
  def monoGraphicImage(dic: SubBook, pos: Long, w: Int, h: Int): Option[Array[Byte]] = {
    val data = try {
      dic.getGraphicData.getMonoGraphic(pos, w, h)
    } catch {
      case _: EBException => return None
    }
    if (ArrayUtils.isEmpty(data)) return None
    Some(ImageUtil.bitmapToPNG(data, w, h, Color.BLACK, Color.WHITE, true, 9))
  }

  /*
  0x00: DIB
  0x01: JPEG
   /**
     * カラー画像を返します。
     *
     * @param subbook 副本
     * @param pos 画像の位置
     * @param type メディアタイプ
     * @return イメージデータ
     */
    private byte[] _getColorImage(SubBook subbook, long pos, int type) {
        byte[] data = null;
        try {
            data = subbook.getGraphicData().getColorGraphic(pos);
        } catch (EBException e) {
            _logger.warn("failed to load color image", e);
            return new byte[0];
        }
        if (ArrayUtils.isEmpty(data)) {
            return new byte[0];
        }
        if (type == PNG) {
            return ImageUtil.dibToPNG(data, 9);
        }
        return data;
    }
   */
  def colorGraphicImage(dic: SubBook, pos: Long, _type: Int): Option[(String, Array[Byte])] = {
    val data = try {
      dic.getGraphicData.getColorGraphic(pos)
    } catch {
      case _: EBException => return None
    }
    if (ArrayUtils.isEmpty(data)) return None
    _type match {
      //convert DIB to PNG
      case 0x00 => Some("png", ImageUtil.dibToPNG(data))
      case _ => Some("jpeg", data)
    }
  }

  /*
  0x01: WAVE
  0x02: MIDI
   /**
     * 音声を返します。
     *
     * @param subbook 副本
     * @param start 開始位置
     * @param end 終了位置
     * @return 音声データ
     */
    private byte[] _getSound(SubBook subbook, int type, long start, long end) {
        byte[] data = null;
        try {
            switch (type) {
                case WAVE:
                    data = subbook.getSoundData().getWaveSound(start, end);
                    break;
                case MIDI:
                    data = subbook.getSoundData().getMidiSound(start, end);
                    break;
                default:
                    break;
            }
        } catch (EBException e) {
            _logger.warn("failed to load sound data", e);
            return new byte[0];
        }
        if (ArrayUtils.isEmpty(data)) {
            return new byte[0];
        }
        return data;
    }
   */
  def soundData(dic: SubBook, _type: Int, start: Long, end: Long): Option[(String, Array[Byte])] = {
    val result = try {
      _type match {
        case 0x01 => ("wav", dic.getSoundData.getWaveSound(start, end))
        case 0x02 => ("mid", dic.getSoundData.getMidiSound(start, end))
        case _ => return None
      }
    } catch {
      case _: EBException => return None
    }

    if (ArrayUtils.isEmpty(result._2)) return None
    Some(result)
  }
}