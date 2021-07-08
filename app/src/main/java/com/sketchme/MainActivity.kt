package com.sketchme

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    // TODO(Step 6 : A variable for current color is picked from color pallet)
    // START
    private var mImageButtonCurrentPaint: ImageButton? = null // A variable for current color is picked from color pallet.
    // END

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(20.toFloat()) // Setting the default brush size to drawing view.

        // TODO(Step 7 : Here we have selected the default color and updated the selected color as an active.)
        // START
        /**
         * This is to select the default Image button which is
         * active and color is already defined in the drawing view class.
         * As the default color is black so in our color pallet it is on 2 position.
         * But the array list start position is 0 so the black color is at position 1.
         */
        mImageButtonCurrentPaint = ll_paint_colors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )
        //END

        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        ib_gallery.setOnClickListener{
            if(isReadStorageAllowed()){
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                startActivityForResult(pickPhotoIntent, GALLERY)
            }else{
                requestStoragePermission()
            }
        }

        ib_undo.setOnClickListener{
            drawing_view.onClickUndo()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                try {
                    if (data!!.data != null){
                        iv_background.visibility = View.VISIBLE
                        iv_background.setImageURI(data.data)
                    }else{
                        Toast.makeText(this@MainActivity, "Error in parsing code or image is corrupted", Toast.LENGTH_SHORT).show()
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Method is used to launch the dialog to select different brush sizes.
     */
    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size :")
        val smallBtn = brushDialog.ib_small_brush
        smallBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        })
        val mediumBtn = brushDialog.ib_medium_brush
        mediumBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        })

        val largeBtn = brushDialog.ib_large_brush
        largeBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        })
        brushDialog.show()
    }

    fun paintClicked(view: View){
        if(view !== mImageButtonCurrentPaint){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawing_view.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_pressed
                )
            )

            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(
                this,
                R.drawable.pallet_normal
                )
            )

            mImageButtonCurrentPaint = view
        }

    }

    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this, "Need permission to add background", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity, "Permission granted! Now you can read the storage files", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@MainActivity, "Permission Denied! Storage files cannot be read", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean{
        val result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View): Bitmap{
        val returnBitMap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnBitMap)
        val bgDrawable = view.background
        if(bgDrawable != null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnBitMap
    }

    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }
}

//Add reset functionality