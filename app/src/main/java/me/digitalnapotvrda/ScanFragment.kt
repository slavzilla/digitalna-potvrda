package me.digitalnapotvrda

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.internal.Objects
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.PDFRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.digitalnapotvrda.barcode.BarcodeProcessor
import me.digitalnapotvrda.camera.CameraSource
import me.digitalnapotvrda.camera.GraphicOverlay
import me.digitalnapotvrda.camera.WorkflowModel
import me.digitalnapotvrda.databinding.FragmentScanBinding
import timber.log.Timber
import java.io.IOException
import java.io.InputStream


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

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    renderToBitmap(uri)
                }
            } else {
                binding.progressGroup.isVisible = false
            }
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

        binding.pdfButton.setOnClickListener {
            startPdfIntent()
        }

        setUpWorkflowModel()
    }

    private fun startPdfIntent() {
        binding.progressGroup.isVisible = true
        stopCameraPreview()
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "application/pdf"
        resultLauncher.launch(intent)
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
                    if (currentQrCode != it) {
                        toast.show()
                        currentQrCode = it
                    }
                }
            }
        })

    }

    private fun startCameraPreview() {
        val cameraSource = this.cameraSource ?: return
        if (!workflowModel.isCameraLive && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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

    private fun renderToBitmap(uri: Uri) {
        GlobalScope.launch(Dispatchers.IO) {
            var bi: Bitmap? = null
            var inputStream: InputStream? = null
            try {
                inputStream = context?.contentResolver?.openInputStream(uri)
                val pd: PDDocument = PDDocument.load(inputStream)
                val pr = PDFRenderer(pd)
                bi = pr.renderImageWithDPI(0, 300f)
            } catch (e: IOException) {
                onPdfError()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    // do nothing because the stream has already been closed
                }
            }
            bi?.let {
                val image = InputImage.fromBitmap(it, 0)
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE
                    )
                    .build()
                val scanner = BarcodeScanning.getClient(options)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val qrCode = barcodes[0].displayValue
                        if (qrCode != null && checkQrCode(qrCode)) {
                            onSuccess(it, qrCode)
                        } else {
                            onPdfError(R.string.invalid_qr_code)
                        }
                    }
                    .addOnFailureListener {
                        onPdfError(R.string.invalid_qr_code)
                    }
            }

        }
    }

    private fun onSuccess(bitmap: Bitmap, qrCode: String) {
        viewModel.setQrCode(qrCode)
        viewModel.setBitmap(bitmap)
        findNavController().navigate(ScanFragmentDirections.scanToQrCode())
    }

    private fun onPdfError(error: Int = R.string.invalid_file) {
        binding.progressGroup.post {
            binding.progressGroup.isVisible = false
            startCameraPreview()
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

}