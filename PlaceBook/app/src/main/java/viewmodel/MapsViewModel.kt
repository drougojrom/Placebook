package viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import model.Bookmark
import repository.BookmarkRepo
import util.ImageUtils

class MapsViewModel(application: Application):
        AndroidViewModel(application) {

    data class BookmarkView(
            var id: Long? = null,
            var location: LatLng = LatLng(0.0, 0.0),
            var name: String = "",
            var phoneNumber: String = "",
            var categoryResourceId: Int? = null
    ) {
        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitmapFromFile(context,
                        Bookmark.generateImageFilename(it))
            }
            return null
        }
    }

    private val TAG = "MapsViewModel"
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarks: LiveData<List<BookmarkView>>? = null

    fun addBookmarkFromPlace(place: Place, image: Bitmap) {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.address = place.address.toString()
        bookmark.latitude = place.latLng.latitude
        bookmark.longitude = place.latLng.longitude
        bookmark.phone = place.phoneNumber.toString()
        bookmark.name = place.name.toString()
        bookmark.category = getPlaceCategory(place)

        val newId = bookmarkRepo.addBookmark(bookmark)
        bookmark.setImage(image, getApplication())

        Log.i(TAG, "New bookmark $newId added")
    }

    fun getBookmarkViews(): LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark): MapsViewModel.BookmarkView {
        return MapsViewModel.BookmarkView(bookmark.id,
                LatLng(bookmark.latitude, bookmark.longitude),
                bookmark.name,
                bookmark.phone,
                bookmarkRepo.getCategoryResourceId(bookmark.category))
    }

    private fun mapBookmarksToBookmarkView() {
        val allBookmarks = bookmarkRepo.allBookmarks
        bookmarks = Transformations.map(allBookmarks) {bookmarks ->
            val bookmarkMarkerViews = bookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
            bookmarkMarkerViews
        }
    }

    private fun getPlaceCategory(place: Place): String {
        var category = "Other"
        val placesTypes = place.placeTypes
        if (placesTypes.size > 0) {
            val placeType = placesTypes[0]
            category = bookmarkRepo.placeTypeToCategory(placeType)
        }
        return  category
    }

}

