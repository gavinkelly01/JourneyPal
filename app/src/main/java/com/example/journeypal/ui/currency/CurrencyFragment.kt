package com.example.journeypal.ui.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.journeypal.databinding.FragmentCurrencyBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.NumberFormat

class CurrencyFragment : Fragment() {
    private var _binding: FragmentCurrencyBinding? = null
    private val binding get() = _binding!!

    private val currencies = listOf(
        "EUR", "USD",  "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR", "BRL",
        "MXN", "NZD", "SEK", "NOK", "DKK", "ZAR", "SGD", "HKD", "KRW", "THB",
        "IDR", "TRY", "RUB", "PLN", "HUF", "CZK", "MYR", "PHP", "AED", "SAR",
        "ILS", "EGP", "PKR", "NGN", "ARS", "CLP", "COP", "TWD", "VND", "BDT"
    )

    private var baseCurrency = "EURO"
    private var targetCurrency = "USD"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrencyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCurrencySpinners()
        setupConversionLogic()
        setupScamPreventionTips()
    }

    private fun setupCurrencySpinners() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            currencies
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.baseCurrencySpinner.adapter = adapter
        binding.baseCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                baseCurrency = currencies[position]
                fetchExchangeRate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.targetCurrencySpinner.adapter = adapter
        binding.targetCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                targetCurrency = currencies[position]
                fetchExchangeRate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupConversionLogic() {
        binding.amountEditText.setOnEditorActionListener { _, _, _ ->
            fetchExchangeRate()
            true
        }
        binding.refreshButton.setOnClickListener {
            fetchExchangeRate()
        }
    }

    private fun setupScamPreventionTips() {
        binding.scamTipsToggle.setOnCheckedChangeListener { _, isChecked ->
            binding.scamTipsContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun fetchExchangeRate() {
        binding.progressBar.visibility = View.VISIBLE
        binding.resultTextView.text = "Fetching exchange rate..."
        val amount = try {
            binding.amountEditText.text.toString().toDoubleOrNull() ?: 100.0
        } catch (e: NumberFormatException) {
            100.0
        }
        lifecycleScope.launch {
            try {
                val rate = getExchangeRate(baseCurrency, targetCurrency)
                val convertedAmount = amount * rate

                // Format results
                val formatter = NumberFormat.getCurrencyInstance()
                formatter.currency = java.util.Currency.getInstance(targetCurrency)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.resultTextView.text = """
                        ${formatter.format(convertedAmount)}
                        
                        Exchange Rate: 
                        1 $baseCurrency = ${String.format("%.4f", rate)} $targetCurrency
                    """.trimIndent()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.resultTextView.text = "Error fetching exchange rate"
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getExchangeRate(base: String, target: String): Double = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://open.exchangerate-api.com/v6/latest/$base")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to fetch exchange rate")

            val jsonResponse = response.body?.string() ?: throw Exception("Empty response")
            val jsonObject = JSONObject(jsonResponse)

            val rates = jsonObject.getJSONObject("rates")
            rates.getDouble(target)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
