package integration

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

import com.twitter.scrooge.ThriftStruct
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TIOStreamTransport


object ThriftSerializer {

  def serializeEvent[T <: ThriftStruct](event: T): ByteBuffer = {
    val out = new ByteArrayOutputStream()
    val transport = new TIOStreamTransport(out)
    val protocol = new TCompactProtocol(transport)
    event.write(protocol)
    ByteBuffer.wrap(out.toByteArray)
  }

}
