package me.digitalnapotvrda

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.internal.Objects
import me.digitalnapotvrda.barcode.BarcodeProcessor
import me.digitalnapotvrda.camera.CameraSource
import me.digitalnapotvrda.camera.GraphicOverlay
import me.digitalnapotvrda.camera.WorkflowModel
import me.digitalnapotvrda.databinding.FragmentScanBinding
import timber.log.Timber
import java.io.IOException

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private val workflowModel by viewModels<WorkflowModel>()
    private var cameraSource: CameraSource? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var currentWorkflowState: WorkflowModel.WorkflowState? = null

    private var currentQrCode = ""
    private val toast: Toast by lazy {
        Toast.makeText(requireContext(), R.string.invalid_qr_code, Toast.LENGTH_SHORT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        graphicOverlay =
            binding.root.findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay).apply {
                cameraSource = CameraSource(this)
            }

        setUpWorkflowModel()
    }

    private fun setUpWorkflowModel() {
        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel.workflowState.observe(viewLifecycleOwner, Observer { workflowState ->
            if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
                return@Observer
            }

            currentWorkflowState = workflowState


            when (workflowState) {
                WorkflowModel.WorkflowState.DETECTING, WorkflowModel.WorkflowState.CONFIRMING -> {
                    startCameraPreview()
                }
                else -> {
                }
            }

        })

        workflowModel.detectedBarcode.observe(viewLifecycleOwner, { barcode ->
            val url = barcode.rawValue
            url?.let {
                if (checkQrCode(it)) {
                    viewModel.setQrCode(it)
                    findNavController().navigate(ScanFragmentDirections.scanToQrCode())
                } else {
                    if (currentQrCode != it){
                        toast.show()
                        currentQrCode = it
                    }
                }
            }
        })

    }

    private fun startCameraPreview() {
        val cameraSource = this.cameraSource ?: return
        if (!workflowModel.isCameraLive) {
            try {
                workflowModel.markCameraLive()
                binding.cameraPreview.start(cameraSource)
            } catch (e: IOException) {
                Timber.e(e, "Failed to start camera preview!")
                cameraSource.release()
                this.cameraSource = null
            }
        }
    }

    private fun stopCameraPreview() {
        if (workflowModel.isCameraLive) {
            workflowModel.markCameraFrozen()
            binding.cameraPreview.stop()
        }
    }

    private fun checkQrCode(s: String): Boolean {
        return (s.contains("https://www.ezdravlje.me/evakcinaverification/#!/verify"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraSource?.release()
        cameraSource = null
    }

    override fun onPause() {
        super.onPause()
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED
        stopCameraPreview()
    }

    override fun onResume() {
        super.onResume()
        workflowModel.markCameraFrozen()
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED
        cameraSource?.setFrameProcessor(BarcodeProcessor(graphicOverlay!!, workflowModel))
        workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING)
    }

}