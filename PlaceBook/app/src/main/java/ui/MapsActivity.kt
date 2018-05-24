package ui

import adapter.BookmarkInfoWindowAdapter
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlacePhotoMetadata
import com.google.android.gms.location.places.Places

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.portfolio.romanustiantcev.placebook.R
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import model.Bookmark
import viewmodel.MapsViewModel

class MapsActivity : AppCompatActivity(),
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {


    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var mapsViewModel: MapsViewModel

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "Google play connection failed: " + connectionResult.errorMessage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setupGoogleApiClient()
        setupLocationClient()
        getCurrentLocation()
        setupMapListeners()
        setupMapsViewModel()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupMapsViewModel() {
        mapsViewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
        createBookmarkMarkerObserver()
    }

    private fun setupGoogleApiClient() {
        googleApiClient = GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, this)
                .addApi(Places.GEO_DATA_API)
                .build()
    }

    private fun setupMapListeners() {
        mMap.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        mMap.setOnPoiClickListener {
            displayPoi(it)
        }
        mMap.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION)
    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        } else {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnCompleteListener {
                if (it.result != null) {
                    val latLng = LatLng(it.result.latitude, it.result.longitude)
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    mMap.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")
                }
            } }
    }

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        Places.GeoDataApi
                .getPlaceById(googleApiClient, pointOfInterest.placeId)
                .setResultCallback { places ->
                    if (places.status.isSuccess && places.count > 0) {
                        val place = places.get(0).freeze()
                        displayPoiGetPhotoMetaDataStep(place)
                    } else {
                        Log.e(TAG, "Error with getPlaceById ${places.status.statusMessage}")
                    }
                    places.release()
                }
    }

    private fun displayPoiGetPhotoMetaDataStep(place: Place) {
        Places.GeoDataApi
                .getPlacePhotos(googleApiClient, place.id)
                .setResultCallback { placePhotoMetadataResult ->
                    if (placePhotoMetadataResult.status.isSuccess) {
                        val photoMetadataBuffer = placePhotoMetadataResult.photoMetadata
                        if (photoMetadataBuffer.count > 0) {
                            val photo = photoMetadataBuffer.get(0).freeze()
                            displayPoiGetPhotoStep(place, photo)
                        }
                        photoMetadataBuffer.release()
                    }
                }
    }

    private fun displayPoiGetPhotoStep(place: Place, photo: PlacePhotoMetadata) {
        photo.getScaledPhoto(googleApiClient,
                resources.getDimensionPixelSize(R.dimen.default_image_width),
                resources.getDimensionPixelSize(R.dimen.default_image_height))
                .setResultCallback { placePhotoResult ->
                    if (placePhotoResult.status.isSuccess) {
                        val image = placePhotoResult.bitmap
                        displayPoiDisplayStep(place, image)
                    } else {
                        displayPoiDisplayStep(place, null)
                    }
        }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        val iconPhoto = if (photo == null) {
            BitmapDescriptorFactory.defaultMarker()
        } else {
            BitmapDescriptorFactory.fromBitmap(photo)
        }

        val marker = mMap.addMarker(MarkerOptions()
                .position(place.latLng)
                .title(place.name as? String)
                .snippet(place.phoneNumber as? String))
        marker.tag = PlaceInfo(place, photo)
        marker?.showInfoWindow()
    }

    private fun handleInfoWindowClick(marker: Marker) {
        val placeInfo = (marker.tag as PlaceInfo)
        if (placeInfo.place != null && placeInfo.image != null) {
            launch(CommonPool) {
                mapsViewModel.addBookmarkFromPlace(place = placeInfo.place, image = placeInfo.image)
            }
        }
        marker.remove()
    }

    private fun addPlaceMarker(bookmark: MapsViewModel.BookmarkMarkerView): Marker? {
        val marker = mMap.addMarker(MarkerOptions()
                .position(bookmark.location)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .alpha(0.8f))
        marker.tag = bookmark
        return marker
    }

    private fun displayAllBookmarks(bookmarks: List<MapsViewModel.BookmarkMarkerView>) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    private fun createBookmarkMarkerObserver() {
        mapsViewModel.getBookmarkMarkerViews()?.observe(this,
                android.arch.lifecycle.Observer<List<MapsViewModel.BookmarkMarkerView>> {
                    mMap.clear()
                    it?.let {
                        displayAllBookmarks(it)
                    }
        })
    }

    class PlaceInfo(val place: Place? = null,
                    val image: Bitmap? = null)

}
