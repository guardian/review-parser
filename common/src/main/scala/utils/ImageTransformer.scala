package utils

import com.gu.contentapi.client.model.v1.{Asset, Element}
import com.gu.contentatom.thrift.{Image, ImageAsset, ImageAssetDimensions}

object ImageTransformer {

  def toAtomImages(imageElements: Seq[Element]): Seq[Image] = {
    imageElements.map { imageElement =>
      val imageAssets = imageElement.assets map toImageAsset
      Image(
        assets = imageAssets,
        master = imageAssets.headOption,
        mediaId = imageElement.id
      )
    }
  }

  private def toImageAsset(asset: Asset): ImageAsset = {
    ImageAsset(
      mimeType = asset.mimeType,
      file = asset.file.getOrElse(""),
      dimensions = asset.typeData.flatMap { td => for (height <- td.height; width <- td.width) yield ImageAssetDimensions(height, width) },
      size = None
    )
  }

}
