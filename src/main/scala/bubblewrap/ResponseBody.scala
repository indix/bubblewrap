package bubblewrap

import java.io.ByteArrayOutputStream

class ResponseBody {
  var bytes = new ByteArrayOutputStream()
  var sizeSoFar = 0
  var encoding = "UTF-8"

  def useCharset(encoding:String) = this.encoding = encoding
  def exceedsSize(max: Long) = sizeSoFar > max
  def write(part:Array[Byte]) = {bytes.write(part); sizeSoFar = sizeSoFar + part.length}
  def content = bytes.toString(encoding)
}