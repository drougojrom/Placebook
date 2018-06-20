package viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import model.Bookmark
import repository.BookmarkRepo
import util.ImageUtils

class BookmarkDetailsViewModel(application: Application):
        AndroidViewModel(application) {

    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarkDetailsView: LiveData<BookmarkDetailsView>? = null

    data class BookmarkDetailsView(
            var id: Long? = null,
            var name: String = "",
            var phone: String = "",
            var address: String = "",
            var notes: String = "",
            var category: String = "",
            var placeId: String? = null,
            val latitude: Double = 0.0,
            val longitude: Double = 0.0
    ) {
        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitmapFromFile(context, Bookmark.generateImageFilename(it))
            }
            return null
        }
        fun setImage(context: Context, image: Bitmap) {
            id?.let {
                ImageUtils.saveBitmapToFile(context, image,
                        Bookmark.generateImageFilename(it))
            } }
    }

    fun getBookmark(bookmarkId: Long): LiveData<BookmarkDetailsView>? {
        if (bookmarkDetailsView == null) {
            mapBookmarkToBookrmarkView(bookmarkId)
        }
        return bookmarkDetailsView
    }

    fun updateBookmark(bookmarkDetailsView: BookmarkDetailsView) {
        launch(CommonPool) {
            val bookmark = bookmarkViewToBookamrk(bookmarkDetailsView)
            bookmark?.let {
                bookmarkRepo.updateBookmark(it)
            }
        }
    }

    fun deleteBookmark(bookmarkDetailsView: BookmarkDetailsView) {
        launch(CommonPool) {
            val bookmark = bookmarkDetailsView.id?.let {
                bookmarkRepo.getBookmark(it)
            }
            bookmark?.let {
                bookmarkRepo.deleteBookmark(it)
            }
        }
    }

    fun getCategoryResourseId(category: String): Int? {
        return bookmarkRepo.getCategoryResourceId(category)
    }

    fun getCategories():List<String> {
        return bookmarkRepo.categories
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark): BookmarkDetailsView {
        return BookmarkDetailsView(bookmark.id,
                bookmark.name,
                bookmark.phone,
                bookmark.address,
                bookmark.notes,
                bookmark.category,
                bookmark.placeId,
                bookmark.latitude,
                bookmark.longitude)
    }

    private fun mapBookmarkToBookrmarkView(bookmarkId: Long) {
        val bookmark = bookmarkRepo.getLiveBookmark(bookmarkId)
        bookmarkDetailsView = Transformations.map(bookmark) { bookmark ->
            bookmark?.let {
                val bookmarkView = bookmarkToBookmarkView(it)
                bookmarkView
            }
        }
    }

    private fun bookmarkViewToBookamrk(bookmarkDetailsView: BookmarkDetailsView): Bookmark? {
        val bookmark = bookmarkDetailsView.id?.let {
            bookmarkRepo.getBookmark(it)
        }
        if (bookmark != null) {
            bookmark.id = bookmarkDetailsView.id
            bookmark.name = bookmarkDetailsView.name
            bookmark.phone = bookmarkDetailsView.phone
            bookmark.address = bookmarkDetailsView.address
            bookmark.notes = bookmarkDetailsView.notes
            bookmark.category = bookmarkDetailsView.category
        }
        return  bookmark
    }
}