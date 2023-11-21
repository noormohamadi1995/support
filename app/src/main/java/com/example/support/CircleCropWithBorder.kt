import android.graphics.*
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.nio.ByteBuffer
import java.security.MessageDigest

class CircleCropWithBorder(private val borderWidth: Int, private val borderColor: Int) : BitmapTransformation() {

    companion object {
        private const val ID = "com.example.CircleCropWithBorder"
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        val radiusData = ByteBuffer.allocate(4).putInt(borderWidth).array()
        messageDigest.update(radiusData)
        val colorData = ByteBuffer.allocate(4).putInt(borderColor).array()
        messageDigest.update(colorData)
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val bitmap = TransformationUtils.circleCrop(pool, toTransform, outWidth, outHeight)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderWidth.toFloat()
            isAntiAlias = true
        }
        val radius = (bitmap.width / 2).toFloat()
        canvas.drawCircle(radius, radius, radius - borderWidth / 2, paint)
        return bitmap
    }
}
