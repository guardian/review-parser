package integration

import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.gu.auxiliaryatom.model.auxiliaryatomevent.v1.AuxiliaryAtomEvent
import com.gu.contentatom.thrift.ContentAtomEvent

import scala.util.{Failure, Success, Try}


object AtomPublisher {

  private object composerAuxiliaryAtomIntegration {

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

  private object porterAtomIntegration {

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

  /**
    * Sends events to Porter and Composer to create new Content and Auxiliary atoms respectively.
    *
    * @param auxiliaryAtomEvent
    * @param contentAtomEvent
    * @param config
    */
  def send(auxiliaryAtomEvent: AuxiliaryAtomEvent, contentAtomEvent: ContentAtomEvent)(config: ReviewParserConfig): Unit = {
    composerAuxiliaryAtomIntegration.send(auxiliaryAtomEvent)(config)
    porterAtomIntegration.send(contentAtomEvent)(config)
  }

}
