package bubblewrap

import java.io.ByteArrayOutputStream

class ResponseBody {
  var bytes = new ByteArrayOutputStream()
  var sizeSoFar = 0

  def exceedsSize(max: Long) = sizeSoFar > max
  def write(part:Array[Byte]) = {bytes.write(part); sizeSoFar = sizeSoFar + part.length}
  def content = bytes.toString("UTF-8")
}