package me.digitalnapotvrda.barcode

import androidx.annotation.MainThread
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import me.digitalnapotvrda.camera.FrameProcessorBase
import me.digitalnapotvrda.camera.GraphicOverlay
import me.digitalnapotvrda.camera.WorkflowModel
import timber.log.Timber
import java.io.IOException

/** A processor to run the barcode detector.  */
class BarcodeProcessor(graphicOverlay: GraphicOverlay, private val workflowModel: WorkflowModel) :
    FrameProcessorBase<List<Barcode>>() {

    private val scanner = BarcodeScanning.getClient()

    override fun detectInImage(image: InputImage): Task<List<Barcode>> =
        scanner.process(image)

    @MainThread
    override fun onSuccess(
        inputInfo: InputInfo,
        results: List<Barcode>,
        graphicOverlay: GraphicOverlay
    ) {

        if (!workflowModel.isCameraLive) return

        Timber.d("Barcode result size: ${results.size}")

        // Picks the barcode, if exists, that covers the center of graphic overlay.

        val barcodeInCenter = results.firstOrNull { barcode ->
            val boundingBox = barcode.boundingBox ?: return@firstOrNull false
            val box = graphicOverlay.translateRect(boundingBox)
            box.contains(graphicOverlay.width / 2f, graphicOverlay.height / 2f)
        }

//        graphicOverlay.clear()

        if (barcodeInCenter == null) {
//            graphicOverlay.add(BarcodeGraphicBase(graphicOverlay))
            workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING)
        } else {
            workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTED)
            workflowModel.detectedBarcode.setValue(barcodeInCenter)
        }
//        graphicOverlay.invalidate()
    }


    override fun onFailure(e: Exception) {
        Timber.e(e, "Barcode detection failed!")
    }

    override fun stop() {
        super.stop()
        try {
            scanner.close()
        } catch (e: IOException) {
            Timber.e(e, "Failed to close barcode detector!")
        }
    }
}