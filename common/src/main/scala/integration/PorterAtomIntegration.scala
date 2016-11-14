package integration

import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.gu.contentatom.thrift.ContentAtomEvent
import scala.util.{Failure, Success, Try}

object PorterAtomIntegration {

  def send(event: ContentAtomEvent)(config: ReviewParserConfig): Unit = {
    val data = ThriftSerializer.serializeEvent(event)
    val record = new PutRecordRequest()
      .withData(data)
      .withStreamName(config.contentAtomConfig.streamName)
      .withPartitionKey(event.atom.atomType.name)

    Try(config.contentAtomConfig.kinesisClient.putRecord(record)) match {
      case Success(_) => println(s"Publishing content atom with id: ${event.atom.id}")
      case Failure(error) => println(s"Failed to publish content atom with id: ${event.atom.id}: ${error.getMessage}", error)
    }
  }

}
