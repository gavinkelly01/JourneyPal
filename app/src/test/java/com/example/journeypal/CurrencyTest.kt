package com.example.journeypal

import com.example.journeypal.ui.currency.CurrencyFragment
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyTest {

    @Test
    fun testParseExchangeRate_success() = runTest {
        val fakeResponse = """
        {
            "rates": {
                "USD": 1.23
            }
        }
        """.trimIndent()

        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(), fakeResponse)
        val response = Response.Builder()
            .request(Request.Builder().url("http://test").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

        val mockCall = mockk<Call>()
        every { mockCall.execute() } returns response

        val mockClient = mockk<OkHttpClient>()
        every { mockClient.newCall(any<Request>()) } returns mockCall

        val fragment = CurrencyFragment().apply {
            injectHttpClient(mockClient)
        }

        val rate = fragment.getExchangeRate("EUR", "USD")
        assertEquals(1.23, rate, 0.001)
    }

    @Test(expected = Exception::class)
    fun testParseExchangeRate_failure() = runTest {
        val response = Response.Builder()
            .request(Request.Builder().url("http://test").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Server Error")
            .body(ResponseBody.create(null, ""))
            .build()

        val mockCall = mockk<Call>()
        every { mockCall.execute() } returns response

        val mockClient = mockk<OkHttpClient>()
        every { mockClient.newCall(any<Request>()) } returns mockCall

        val fragment = CurrencyFragment().apply {
            injectHttpClient(mockClient)
        }

        fragment.getExchangeRate("EUR", "USD")
    }
}
