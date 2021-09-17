package me.digitalnapotvrda

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import me.digitalnapotvrda.databinding.FragmentQrCodeBinding
import me.digitalnapotvrda.utils.showYesOrNoDialog
import java.util.*


class QrCodeFragment : Fragment() {

    private var _binding: FragmentQrCodeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.qrCode.apply {
            val qrCode = viewModel.getQrCode()
            val multiFormatWriter = MultiFormatWriter()
            val width = resources.displayMetrics.widthPixels
            try {
                val hintMap: MutableMap<EncodeHintType, Any?> =
                    EnumMap(EncodeHintType::class.java)
                hintMap[EncodeHintType.MARGIN] = 1
                val matrix = multiFormatWriter.encode(
                    qrCode,
                    BarcodeFormat.QR_CODE, width, width, hintMap
                )
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.createBitmap(matrix)
                setImageBitmap(bitmap)
                visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(qrCode)))
            }
        }
        binding.delete.setOnClickListener {
            context?.showYesOrNoDialog(R.string.delete, {
                viewModel.invalidateQrCode()
                findNavController().navigate(QrCodeFragmentDirections.qrCodeToScan())
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}