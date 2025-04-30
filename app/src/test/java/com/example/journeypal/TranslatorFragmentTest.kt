package com.example.journeypal

import android.app.Application
import android.view.View
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.example.journeypal.ui.translator.TranslatorFragment
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Ensure correct test runner is used.
@Config(
    manifest = Config.NONE,
    sdk = [28],
    application = Application::class
)
@RunWith(RobolectricTestRunner::class)
class TranslatorFragmentTest {

    private lateinit var scenario: FragmentScenario<TranslatorFragment>

    @Before
    fun setup() {
        // Launch the fragment inside the container
        scenario = launchFragmentInContainer(
            themeResId = androidx.appcompat.R.style.Theme_AppCompat
        )
    }

    @Test
    fun testInitialLanguageSelection() {
        scenario.onFragment { fragment ->
            val sourceSpinner = fragment.view?.findViewById<Spinner>(R.id.sourceLanguageSpinner)
            val targetSpinner = fragment.view?.findViewById<Spinner>(R.id.targetLanguageSpinner)

            assertNotNull("Source language spinner not found", sourceSpinner)
            assertNotNull("Target language spinner not found", targetSpinner)

            val sourceLang = sourceSpinner?.selectedItem?.toString()
            val targetLang = targetSpinner?.selectedItem?.toString()

            assertFalse("Source language is null", sourceLang.isNullOrBlank())
            assertFalse("Target language is null", targetLang.isNullOrBlank())
        }
    }

    @Test
    fun testSwapLanguages() {
        scenario.onFragment { fragment ->
            val sourceSpinner = fragment.view?.findViewById<Spinner>(R.id.sourceLanguageSpinner)
            val targetSpinner = fragment.view?.findViewById<Spinner>(R.id.targetLanguageSpinner)
            val swapButton = fragment.view?.findViewById<Button>(R.id.swapLanguagesButton)

            assertNotNull(sourceSpinner)
            assertNotNull(targetSpinner)
            assertNotNull(swapButton)

            sourceSpinner?.setSelection(0)
            targetSpinner?.setSelection(1)
            val beforeSource = sourceSpinner?.selectedItemPosition
            val beforeTarget = targetSpinner?.selectedItemPosition

            swapButton?.performClick()

            assertEquals(
                "Source and target languages did not swap as expected",
                beforeSource, targetSpinner?.selectedItemPosition
            )
            assertEquals(
                "Target and source languages did not swap as expected",
                beforeTarget, sourceSpinner?.selectedItemPosition
            )
        }
    }

    @Test
    fun testSwitchToTextMode() {
        scenario.onFragment { fragment ->
            fragment.setTranslationMode(TranslatorFragment.TranslationMode.TEXT)

            val inputLayout = fragment.view?.findViewById<View>(R.id.sourceTextLayout)
            val preview = fragment.view?.findViewById<View>(R.id.cameraPreviewView)
            val translateButton = fragment.view?.findViewById<Button>(R.id.translateButton)

            assertEquals(View.VISIBLE, inputLayout?.visibility)
            assertEquals(View.GONE, preview?.visibility)
            assertEquals(View.VISIBLE, translateButton?.visibility)
        }
    }
}
