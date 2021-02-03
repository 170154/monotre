package com.example.monotre

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.roundToInt

data class ListItem(var thumbnail: Bitmap? = null,
                    var itemName : String,
                    var distance : String,
                    var strength : Bitmap? = null) {
    companion object {
        fun builder(resources: Resources): Builder =
                Builder(resources)
        class Builder(val resources: Resources) {
            fun build(thumbnail: Bitmap? = null,
                      itemName : String,
                      distance : String): ListItem {

                val roundedDistance = (distance.toDouble() * 100).roundToInt().toDouble() / 100
                val strength: Bitmap? =
                        when{
                            roundedDistance <= 0.5
                            -> BitmapFactory.decodeResource(resources, R.mipmap.strength5)

                            roundedDistance <= 1.0
                            -> BitmapFactory.decodeResource(resources, R.mipmap.strength4)

                            roundedDistance <= 1.5
                            -> BitmapFactory.decodeResource(resources, R.mipmap.strength3)

                            roundedDistance <= 2.0
                            -> BitmapFactory.decodeResource(resources, R.mipmap.strength2)

                            else
                            -> BitmapFactory.decodeResource(resources, R.mipmap.strength1)
                        }
                return ListItem(thumbnail, itemName, roundedDistance.toString(), strength)
            }
        }
    }
}
