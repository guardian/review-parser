package integration

import java.io.File

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider.Builder
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.gu.contentapi.client.GuardianContentClient
import com.typesafe.config.ConfigFactory

import scala.util.Try

case class ContentAtomConfig(streamName: String, stsRoleArn: String, kinesisClient: AmazonKinesisClient)
case class AuxiliaryAtomConfig(streamName: String, stsRoleArn: String, kinesisClient: AmazonKinesisClient)
case class CapiConfig(capiApiKey: String, capiClient: GuardianContentClient)
case class ReviewParserConfig(contentAtomConfig: ContentAtomConfig, auxiliaryAtomConfig: AuxiliaryAtomConfig, capiConfig: CapiConfig)


object ReviewParserConfig {

  private val userHome = System.getProperty("user.home")
  private val rootConfig = Try(ConfigFactory.parseFile(new File(s"$userHome/.gu/review-parser-common.conf"))).toOption getOrElse sys.error("Could not find config file. This application will not run.")

  def apply(stage: String): ReviewParserConfig = {

    val conf = Try(rootConfig.getConfig(s"review-parser.${stage.toLowerCase}")).toOption getOrElse sys.error("Could not retrieve stage sensitive config. This application will not run.")

    def getMandatoryString(item: String) = Try(conf.getString(item)).toOption getOrElse sys.error(s"Could not get item $item from config. Exiting.")

    val contentAtomStreamName = getMandatoryString("contentAtom.streamName")
    val contentAtomStsRoleArn = getMandatoryString("contentAtom.stsRoleArn")

    val contentAtomConfig = ContentAtomConfig(
      contentAtomStreamName,
      contentAtomStsRoleArn,
      kinesisClient = {
        val kinesisCredentialsProvider = new AWSCredentialsProviderChain(
          new ProfileCredentialsProvider("composer"),
          new Builder(contentAtomStsRoleArn, "contentAtom").build()
        )

        val kinesisClient = new AmazonKinesisClient(kinesisCredentialsProvider)
        kinesisClient.setRegion(Region getRegion Regions.fromName("eu-west-1"))
        kinesisClient
      }
    )

    val auxiliaryAtomStreamName = getMandatoryString("auxiliaryAtom.streamName")
    val auxiliaryAtomStsRoleArn = getMandatoryString("auxiliaryAtom.stsRoleArn")

    val auxiliaryAtomConfig = AuxiliaryAtomConfig(
      auxiliaryAtomStreamName,
      auxiliaryAtomStsRoleArn,
      kinesisClient = {
        val kinesisCredentialsProvider = new AWSCredentialsProviderChain(
          new ProfileCredentialsProvider("composer"),
          new Builder(auxiliaryAtomStsRoleArn, "auxiliaryAtom").build()
        )

        val kinesisClient = new AmazonKinesisClient(kinesisCredentialsProvider)
        kinesisClient.setRegion(Region getRegion Regions.fromName("eu-west-1"))
        kinesisClient
      }
    )

    val capiApiKey = getMandatoryString("capi.apiKey")
    val capiConfig = CapiConfig(capiApiKey, capiClient = new GuardianContentClient(capiApiKey))


    ReviewParserConfig(contentAtomConfig, auxiliaryAtomConfig, capiConfig)
  }

}
