package repository

import android.arch.lifecycle.LiveData
import android.content.Context
import db.BookmarkDao
import db.PlaceBookDatabase
import model.Bookmark

class BookmarkRepo(private val context: Context) {
    private var db: PlaceBookDatabase = PlaceBookDatabase.Companion.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()

    fun addBookmark(bookmark: Bookmark): Long {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }

    fun createBookmark(): Bookmark {
        return Bookmark()
    }

    fun getLiveBookmark(id: Long): LiveData<Bookmark> {
        val bookmark = bookmarkDao.loadLiveBookmark(id)
        return bookmark
    }

    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }

    fun getBookmark(id: Long): Bookmark {
        return bookmarkDao.loadBookmark(id)
    }

    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }
}