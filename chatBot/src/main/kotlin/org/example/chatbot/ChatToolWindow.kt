import javax.swing.*
import java.awt.BorderLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class ChatToolWindow : JPanel(BorderLayout()) {
    private val chatArea = JTextArea()
    private val inputField = JTextField()

    init {
        chatArea.isEditable = false
        add(JScrollPane(chatArea), BorderLayout.CENTER)
        add(inputField, BorderLayout.SOUTH)

        inputField.addActionListener { _ ->
            val message = inputField.text
            if (message.isNotBlank()) {
                addMessage("You: $message")
                inputField.text = ""
                handleUserMessage(message)
            }
        }
    }

    private fun addMessage(message: String) {
        chatArea.append("$message\n")
    }

    private fun handleUserMessage(message: String) {
        getAIResponse(message) { botResponse ->
            addMessage("Bot: $botResponse")
        }
    }

    private fun getAIResponse(userMessage: String, callback: (String) -> Unit) {
        val apiKey = "YOUR_API_KEY"
        val client = OkHttpClient()
        val url = "https://api.openai.com/v1/engines/davinci-codex/completions"

        // Создаём тело запроса
        val requestBody = JSONObject().apply {
            put("prompt", userMessage)
            put("max_tokens", 50)
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val responseJson = JSONObject(responseBody)
                        val botResponse = responseJson.getJSONArray("choices").getJSONObject(0).getString("text").trim()
                        callback(botResponse)
                    }
                } else {
                    callback("Response error")
                }
            }
        })
    }


    fun getContent() = this
}
