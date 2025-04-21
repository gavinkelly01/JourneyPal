package com.example.journeypal.ui.translator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.journeypal.databinding.FragmentTranslatorBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TranslatorFragment : Fragment() {

    private var _binding: FragmentTranslatorBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var textRecognizer: TextRecognizer
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionListener: RecognitionListener? = null

    private var sourceLanguage = "EN"
    private var targetLanguage = "ES"
    private var translationMode = TranslationMode.TEXT

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            when (translationMode) {
                TranslationMode.VOICE -> setupVoiceRecognition()
                TranslationMode.CAMERA -> startCameraPreview()
                else -> {}
            }
        } else {
            Toast.makeText(requireContext(), "Permissions required to use this feature", Toast.LENGTH_SHORT).show()
        }
    }

    enum class TranslationMode { TEXT, VOICE, CAMERA }

    companion object {
        private const val DEEPL_API_KEY = "3f7b78e8-7d12-46db-a4a5-c72bd4f68d33:fx"
        private const val DEEPL_API_URL = "https://api-free.deepl.com/v2/translate"
    }

    private val languages = mapOf(
        "English" to "EN", "Spanish" to "ES", "French" to "FR",
        "German" to "DE", "Italian" to "IT", "Japanese" to "JA",
        "Korean" to "KO", "Chinese" to "ZH", "Russian" to "RU", "Arabic" to "AR"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTranslatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        } else {
            Toast.makeText(requireContext(), "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }

        setupLanguageSpinners()
        setupModeSwitching()
        setupTranslationButton()
        setTranslationMode(TranslationMode.TEXT)
    }

    private fun setupLanguageSpinners() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.sourceLanguageSpinner.adapter = adapter
        binding.targetLanguageSpinner.adapter = adapter

        binding.sourceLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val selectedLanguage = binding.sourceLanguageSpinner.selectedItem.toString()
                sourceLanguage = languages[selectedLanguage] ?: "EN"
            }

            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        binding.targetLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val selectedLanguage = binding.targetLanguageSpinner.selectedItem.toString()
                targetLanguage = languages[selectedLanguage] ?: "ES"
            }

            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun setupModeSwitching() {
        binding.textModeButton.setOnClickListener {
            setTranslationMode(TranslationMode.TEXT)
        }

        binding.voiceModeButton.setOnClickListener {
            setTranslationMode(TranslationMode.VOICE)
            checkAndRequestVoicePermissions()
        }

        binding.cameraModeButton.setOnClickListener {
            setTranslationMode(TranslationMode.CAMERA)
            checkAndRequestCameraPermissions()
        }

        binding.swapLanguagesButton.setOnClickListener {
            val sourcePos = binding.sourceLanguageSpinner.selectedItemPosition
            binding.sourceLanguageSpinner.setSelection(binding.targetLanguageSpinner.selectedItemPosition)
            binding.targetLanguageSpinner.setSelection(sourcePos)

            if (translationMode == TranslationMode.TEXT && binding.translatedTextView.text.isNotEmpty()) {
                val originalText = binding.sourceTextEditText.text.toString()
                binding.sourceTextEditText.setText(binding.translatedTextView.text)
                binding.translatedTextView.text = originalText
            }
        }
    }

    private fun setupTranslationButton() {
        binding.translateButton.setOnClickListener {
            if (translationMode == TranslationMode.TEXT) {
                val input = binding.sourceTextEditText.text.toString()
                if (input.isNotEmpty()) translateText(input)
            }
        }

        binding.translateButton.setOnTouchListener { v, event ->
            if (translationMode == TranslationMode.VOICE) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startVoiceRecognition()
                        v.isPressed = true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        stopVoiceRecognition()
                        v.isPressed = false
                    }
                }
            }
            true
        }
    }

    private fun setTranslationMode(mode: TranslationMode) {
        translationMode = mode
        binding.sourceTextLayout.visibility = View.GONE
        binding.voiceInstructions.visibility = View.GONE
        binding.cameraPreviewView.visibility = View.GONE
        binding.translateButton.visibility = View.VISIBLE

        val context = requireContext()
        val selectedColor = ContextCompat.getColor(context, android.R.color.holo_blue_light)
        val defaultColor = ContextCompat.getColor(context, android.R.color.darker_gray)

        binding.textModeButton.setBackgroundColor(if (mode == TranslationMode.TEXT) selectedColor else defaultColor)
        binding.voiceModeButton.setBackgroundColor(if (mode == TranslationMode.VOICE) selectedColor else defaultColor)
        binding.cameraModeButton.setBackgroundColor(if (mode == TranslationMode.CAMERA) selectedColor else defaultColor)

        when (mode) {
            TranslationMode.TEXT -> {
                binding.sourceTextLayout.visibility = View.VISIBLE
                binding.translateButton.text = "Translate Text"
            }
            TranslationMode.VOICE -> {
                binding.voiceInstructions.visibility = View.VISIBLE
                binding.translateButton.text = "Hold to Speak"
                binding.voiceInstructions.text = "Press and hold to speak, release to translate"
            }
            TranslationMode.CAMERA -> {
                binding.cameraPreviewView.visibility = View.VISIBLE
                binding.translateButton.visibility = View.GONE
                startCameraPreview()
            }
        }
    }

    private fun translateText(text: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.translatedTextView.text = "Translating..."

        lifecycleScope.launch {
            try {
                val result = getDeepLTranslation(text, sourceLanguage, targetLanguage)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.translatedTextView.text = result
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.translatedTextView.text = "Translation failed"
                    Toast.makeText(requireContext(), "Translation error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getDeepLTranslation(text: String, source: String, target: String): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val formBody = listOfNotNull(
            "text" to text,
            if (source != "AUTO") "source_lang" to source else null,
            "target_lang" to target
        ).joinToString("&") { "${it.first}=${it.second}" }

        val requestBody = formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType())

        val request = Request.Builder()
            .url(DEEPL_API_URL)
            .addHeader("Authorization", "DeepL-Auth-Key $DEEPL_API_KEY")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Translation failed: ${response.code}")
            val jsonResponse = JSONObject(response.body?.string() ?: "")
            val translations = jsonResponse.getJSONArray("translations")
            translations.getJSONObject(0).getString("text")
        }
    }

    private fun setupVoiceRecognition() {
        if (speechRecognizer == null) {
            Toast.makeText(requireContext(), "Speech recognition not available", Toast.LENGTH_SHORT).show()
            return
        }

        if (recognitionListener == null) {
            recognitionListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    binding.voiceInstructions.text = "Speak now..."
                }

                override fun onBeginningOfSpeech() {
                    binding.voiceInstructions.text = "Listening..."
                }

                override fun onEndOfSpeech() {
                    binding.voiceInstructions.text = "Processing speech..."
                }

                override fun onResults(results: Bundle?) {
                    processVoiceResults(results)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialText = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!partialText.isNullOrEmpty()) {
                        binding.voiceInstructions.text = "Hearing: $partialText"
                    }
                }

                override fun onError(error: Int) {
                    binding.progressBar.visibility = View.GONE
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        else -> "Error code: $error"
                    }
                    binding.voiceInstructions.text = "Voice error: $errorMessage"
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
        }

        speechRecognizer?.setRecognitionListener(recognitionListener)
    }

    private fun startVoiceRecognition() {
        if (speechRecognizer == null) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLocaleFromLanguageCode(sourceLanguage).toString())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.translatedTextView.text = ""
        speechRecognizer?.startListening(intent)
    }

    private fun stopVoiceRecognition() {
        speechRecognizer?.stopListening()
    }

    private fun processVoiceResults(results: Bundle?) {
        val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
        binding.progressBar.visibility = View.GONE
        if (!spokenText.isNullOrEmpty()) {
            binding.sourceTextEditText.setText(spokenText)
            binding.voiceInstructions.text = "Recognized: $spokenText"
            translateText(spokenText)
        } else {
            binding.voiceInstructions.text = "No speech recognized"
        }
    }

    private fun checkAndRequestVoicePermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            setupVoiceRecognition()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun checkAndRequestCameraPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun startCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
            }
            val analyzer = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                .also {
                    it.setAnalyzer(cameraExecutor, TextAnalyzer { detectedText ->
                        if (detectedText.isNotEmpty()) translateText(detectedText)
                    })
                }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    inner class TextAnalyzer(private val onTextFound: (String) -> Unit) : ImageAnalysis.Analyzer {
        private var lastAnalyzedTime = 0L

        @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastAnalyzedTime >= 3000) {
                imageProxy.image?.let { mediaImage ->
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    textRecognizer.process(image)
                        .addOnSuccessListener {
                            if (it.text.isNotEmpty()) {
                                onTextFound(it.text)
                                lastAnalyzedTime = currentTime
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } ?: imageProxy.close()
            } else {
                imageProxy.close()
            }
        }
    }

    private fun getLocaleFromLanguageCode(code: String): Locale = when (code) {
        "EN" -> Locale.ENGLISH
        "ES" -> Locale("es")
        "FR" -> Locale("fr")
        "DE" -> Locale("de")
        "IT" -> Locale("it")
        "JA" -> Locale("ja")
        "KO" -> Locale("ko")
        "ZH" -> Locale.SIMPLIFIED_CHINESE
        "RU" -> Locale("ru")
        "AR" -> Locale("ar")
        else -> Locale.ENGLISH
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
        speechRecognizer?.destroy()
    }
}
