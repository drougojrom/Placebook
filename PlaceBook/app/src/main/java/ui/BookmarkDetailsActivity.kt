package ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.portfolio.romanustiantcev.placebook.R
import kotlinx.android.synthetic.main.activity_bookmark_details.*

class BookmarkDetailsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

}