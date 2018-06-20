package ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.portfolio.romanustiantcev.placebook.R
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import kotlinx.android.synthetic.main.drawer_view_maps.*
import viewmodel.BookmarkDetailsViewModel
import ui.PhotoOptionDialogFragment
import util.ImageUtils
import java.io.File

class BookmarkDetailsActivity: AppCompatActivity(),
        PhotoOptionDialogFragment.PhotoOptionDialogListener {

    private lateinit var bookmarkDetailsViewModel: BookmarkDetailsViewModel
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
        setupViewModel()
        getIntentData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> {
                saveChanges()
                return true
            }
            R.id.action_delete -> {
                deleteBookmark()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == android.app.Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAPTURE_IMAGE -> {
                    val photoFile = photoFile ?: return
                    val uri = FileProvider
                            .getUriForFile(this,
                                    "com.portfolio.romanustiantcev.placebook.fileprovider",
                                    photoFile)
                    revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    val image = getImageWithPath(photoFile.absolutePath)
                    image?.let {
                        updateImage(it)
                    }
                }
                REQUEST_GALLERY_IMAGE -> if (data != null && data.data != null) {
                    val imageUri = data.data
                    val image = getImageWithAuthority(imageUri)
                    image?.let { updateImage(it) }
                }
            }
        }
    }

    // MARK: PhotoDialog listener
    override fun onCaptureClick() {
        photoFile = null
        try {
            photoFile = ImageUtils.createUniqueImageFile(this)
            if (photoFile == null) {
                return
            }
        } catch (ex: java.io.IOException) {
            return
        }
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoUri = FileProvider.getUriForFile(this,
                "com.portfolio.romanustiantcev.placebook.fileprovider",
                photoFile)
        captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
        val intentActivities = packageManager.queryIntentActivities(
                captureIntent, PackageManager.MATCH_DEFAULT_ONLY)

        intentActivities.map { it.activityInfo.packageName }
                .forEach { grantUriPermission(it, photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }

        startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickClick() {
        val pickIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupViewModel() {
        bookmarkDetailsViewModel = ViewModelProviders
                .of(this)
                .get(BookmarkDetailsViewModel::class.java)
    }

    private fun populateFields() {
        bookmarkDetailsView?.let {
            editTextName.setText(it.name)
            editTextPhone.setText(it.phone)
            editTextAddress.setText(it.address)
            editTextNotes.setText(it.notes)
        }
    }

    private fun populateImageView() {
        bookmarkDetailsView?.let {
            val placeImage = it.getImage(this)
            placeImage?.let {
                imageViewPlace.setImageBitmap(placeImage)
                imageViewPlace.setOnClickListener {
                    replaceImage()
                }
            }
        }
    }

    private fun getIntentData() {
        val bookmarkId = intent.getLongExtra(MapsActivity.Companion.EXTRA_BOOKMARK_ID,
                0)
        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(this,
                Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {
                    it?.let {
                        bookmarkDetailsView = it
                        populateFields()
                        populateImageView()
                        populateCategoryList()
                    }
                })
    }

    private fun saveChanges() {
        val name = editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }

        bookmarkDetailsView?.let {
            it.name = editTextName.text.toString()
            it.address = editTextAddress.text.toString()
            it.notes = editTextNotes.text.toString()
            it.phone = editTextPhone.text.toString()
            it.category = spinnerCategory.selectedItem as String
            bookmarkDetailsViewModel.updateBookmark(it)
        }
        finish()
    }

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    private fun updateImage(image: Bitmap) {
        val bookmarkView = bookmarkDetailsView ?: return
        imageViewPlace.setImageBitmap(image)
        bookmarkView.setImage(this, image)
    }

    private fun getImageWithPath(filePath: String): Bitmap? {
        return ImageUtils.decodeFileToSize(filePath,
                resources.getDimensionPixelSize(
                        R.dimen.default_image_width),
                resources.getDimensionPixelSize(
                        R.dimen.default_image_height))
    }

    private fun getImageWithAuthority(uri: Uri): Bitmap? {
        return ImageUtils.decodeUriStreamToSize(uri,
                resources.getDimensionPixelSize(
                        R.dimen.default_image_width),
                resources.getDimensionPixelSize(
                        R.dimen.default_image_height),
                this)
    }

    private fun populateCategoryList() {
        val bookmarkView = bookmarkDetailsView ?: return
        val resourceId = bookmarkDetailsViewModel.getCategoryResourseId(bookmarkView.category)
        resourceId?.let {
            imageViewCategory.setImageResource(it)
        }
        val categories = bookmarkDetailsViewModel.getCategories()
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item,
                categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        val placeCategory = bookmarkView.category
        spinnerCategory.setSelection(adapter.getPosition(placeCategory))
        spinnerCategory.post {
            spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View,
                                            position: Int, id: Long) {
                    val category = parent.getItemAtPosition(position) as String
                    val resourseId = bookmarkDetailsViewModel.getCategoryResourseId(category)
                    resourceId?.let {
                        imageViewCategory.setImageResource(it)
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }
    }

    fun deleteBookmark() {
        val bookmarkView = bookmarkDetailsView ?: return
        AlertDialog.Builder(this)
                .setMessage("Delete?")
                .setPositiveButton("Ok") { _ , _  ->
                    bookmarkDetailsViewModel.deleteBookmark(bookmarkView)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
    }

    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }
}