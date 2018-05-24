package viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import model.Bookmark
import repository.BookmarkRepo

class MapsViewModel(application: Application):
        AndroidViewModel(application) {

    data class BookmarkMarkerView(
            var id: Long? = null,
            var location: LatLng = LatLng(0.0, 0.0)
    )

    private val TAG = "MapsViewModel"
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarks: LiveData<List<BookmarkMarkerView>>? = null

    fun addBookmarkFromPlace(place: Place, image: Bitmap) {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.address = place.address.toString()
        bookmark.latitude = place.latLng.latitude
        bookmark.longitude = place.latLng.longitude
        bookmark.phone = place.phoneNumber.toString()

        val newId = bookmarkRepo.addBookmark(bookmark)
        bookmark.setImage(image, getApplication())

        Log.i(TAG, "New bookmark $newId added")
    }

    fun getBookmarkMarkerViews(): LiveData<List<BookmarkMarkerView>>? {
        if (bookmarks == null) {
            mapBookmarksToMarkerView()
        }
        return bookmarks
    }

    private fun bookmarkToMarkerView(bookmark: Bookmark): MapsViewModel.BookmarkMarkerView {
        return MapsViewModel.BookmarkMarkerView(bookmark.id,
                LatLng(bookmark.latitude, bookmark.longitude))
    }

    private fun mapBookmarksToMarkerView() {
        val allBookmarks = bookmarkRepo.allBookmarks
        bookmarks = Transformations.map(allBookmarks) {bookmarks ->
            val bookmarkMarkerViews = bookmarks.map { bookmark ->
                bookmarkToMarkerView(bookmark)
            }
            bookmarkMarkerViews
        }
    }

}

