package viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.location.places.Place
import repository.BookmarkRepo

class MapsViewModel(application: Application):
        AndroidViewModel(application) {
    private val TAG = "MapsViewModel"
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())

    fun addBookmarkFromPlace(place: Place, image: Bitmap) {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.address = place.address.toString()
        bookmark.latitude = place.latLng.latitude
        bookmark.longitude = place.latLng.longitude
        bookmark.phone = place.phoneNumber.toString()

        val newId = bookmarkRepo.addBookmark(bookmark)
        Log.i(TAG, "New bookmark $newId added")
    }
}