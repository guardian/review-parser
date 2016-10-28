package integration

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.gu.contentatom.thrift.ContentAtomEvent
import com.twitter.scrooge.ThriftStruct
import com.typesafe.scalalogging.StrictLogging
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TIOStreamTransport

import scala.util.{Failure, Success, Try}

object PorterAtomIntegration extends StrictLogging {

  def send(event: ContentAtomEvent, streamName: String)(kinesisClient: AmazonKinesisClient): Unit = {
    val data = ThriftSerializer.serializeEvent(event)
    val record = new PutRecordRequest()
      .withData(data)
      .withStreamName(streamName)
      .withPartitionKey(event.atom.atomType.name)

    Try(kinesisClient.putRecord(record)) match {
      case Success(_) => logger.info(s"Publishing atom with id: ${event.atom.id}")
      case Failure(error) => logger.error(s"Failed to publish atom with id: ${event.atom.id}: ${error.getMessage}", error)
    }
  }

}

object ThriftSerializer {

  private val compressionByte = 0

  def serializeEvent[T <: ThriftStruct](event: T): ByteBuffer = {
    val out = new ByteArrayOutputStream()
    val transport = new TIOStreamTransport(out)
    val protocol = new TCompactProtocol(transport)
    out.write(compressionByte)
    event.write(protocol)
    ByteBuffer.wrap(out.toByteArray)
  }

}
