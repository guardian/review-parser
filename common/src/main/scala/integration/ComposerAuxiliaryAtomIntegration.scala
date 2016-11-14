package integration

import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.gu.auxiliaryatom.model.auxiliaryatomevent.v1.AuxiliaryAtomEvent
import scala.util.{Failure, Success, Try}

object ComposerAuxiliaryAtomIntegration {

  def send(event: AuxiliaryAtomEvent)(config: ReviewParserConfig): Unit = {
    val data = ThriftSerializer.serializeEvent(event)
    val record = new PutRecordRequest()
      .withData(data)
      .withStreamName(config.auxiliaryAtomConfig.streamName)
      .withPartitionKey(event.contentId)

    Try(config.auxiliaryAtomConfig.kinesisClient.putRecord(record)) match {
      case Success(_) => println(s"Publishing auxiliary atom with id: ${event.auxiliaryAtom.map(_.atomId).mkString(",")} for content ${event.contentId}")
      case Failure(error) => println(s"Failed to publish auxiliary atom with id: ${event.auxiliaryAtom.map(_.atomId).mkString(",")} for content ${event.contentId}: ${error.getMessage}", error)
    }
  }

}
