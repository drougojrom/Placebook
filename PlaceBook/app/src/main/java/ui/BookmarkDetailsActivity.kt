package ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.portfolio.romanustiantcev.placebook.R
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import viewmodel.BookmarkDetailsViewModel

class BookmarkDetailsActivity: AppCompatActivity() {

    private lateinit var bookmarkDetailsViewModel: BookmarkDetailsViewModel
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null

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
            else -> return super.onOptionsItemSelected(item)
        }
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
            bookmarkDetailsViewModel.updateBookmark(it)
        }
        finish()
    }
}