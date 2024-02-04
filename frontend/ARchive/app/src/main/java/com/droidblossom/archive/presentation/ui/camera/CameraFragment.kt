package com.droidblossom.archive.presentation.ui.camera

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.droidblossom.archive.R
import com.droidblossom.archive.databinding.FragmentCameraBinding
import com.droidblossom.archive.presentation.base.BaseFragment
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import dagger.hilt.android.AndroidEntryPoint
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CameraFragment : BaseFragment<CameraViewModelImpl, FragmentCameraBinding>(R.layout.fragment_camera) {

    override val viewModel: CameraViewModelImpl by viewModels<CameraViewModelImpl>()

    private var anchorNode: AnchorNode? = null
    private lateinit var session: Session
    private lateinit var config: Config

    override fun observeData() {
    }

    val permissionList = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        createSession()

    }

    private fun initView(){
        with(binding){
            sceneView.configureSession { session, config ->
                config.geospatialMode = Config.GeospatialMode.ENABLED
                //config.planeFindingMode = Config.PlaneFindingMode.DISABLED
            }

            sceneView.onSessionUpdated = { session, frame ->
                if(anchorNode == null){
                    frame.getUpdatedPlanes().firstOrNull {it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                        ?.let {plane ->
                            Log.d("CameraFragmentAR", "earth setting start")
                            val earth = session.earth
                            if(earth == null){
                                Log.d("CameraFragmentAR", "earth is null")
                                return@let
                            }else{
                                Log.d("CameraFragmentAR", "earth.trackingState = ${earth.cameraGeospatialPose.latitude}")
                                if(earth.trackingState == TrackingState.TRACKING){
                                    val altitude = earth.cameraGeospatialPose.altitude
                                    // Put the anchor somewhere around the user.
                                    val latitude = earth.cameraGeospatialPose.latitude
                                    val longitude = earth.cameraGeospatialPose.longitude
                                    val earthAnchor = earth.createAnchor(latitude, longitude, altitude, 0f,0f,0f,0f)
                                    addAnchorNode(earthAnchor)
                                }
                            }
                        }
                }

            }
        }
    }

    private fun addAnchorNode(anchor: Anchor) {
        Log.d("CameraFragmentAR", "addAnchorNode added ")
        binding.sceneView.addChildNode(
            AnchorNode(binding.sceneView.engine, anchor)
                .apply {
                    lifecycleScope.launch {
                        binding.sceneView.modelLoader.loadModelInstance(
                            "https://sceneview.github.io/assets/models/DamagedHelmet.glb"
                        )?.let { modelInstance ->

                            addChildNode(
                                node =
                                ModelNode(
                                    modelInstance = modelInstance,
                                )
                                    .apply {
                                        playAnimation(0)
                                    }
                            ).apply {
                            }
                        }
                    }
                    anchorNode = this
                }
        )
    }

    private fun createSession(){
        session = Session(requireContext())
        config = Config(session)
        config.geospatialMode = Config.GeospatialMode.ENABLED
        session.configure(config)
    }

    companion object{
        
        const val TAG = "CAMERA"
        fun newIntent()= CameraFragment()
    }
}