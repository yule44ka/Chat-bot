import javax.swing.*
import java.awt.BorderLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Properties

/**
 * ChatToolWindow class creates a chat window panel for interacting with an AI model.
 * It provides a text area to display chat history and an input field for sending messages.
 */
class ChatToolWindow : JPanel(BorderLayout()) {
    private val chatArea = JTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }
    private val inputField = JTextField()
    private val apiKey = loadApiKey()
    private val messages = mutableListOf<JSONObject>() // Stores the message history

    init {
        val systemMessage = JSONObject().apply {
            put("role", "system")
            put("content", "You are a helpful assistant that responds to user questions in IntelliJ IDEA. Do not use markdown.")
        }
        messages.add(systemMessage)

        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        // Add JScrollPane for the chat area
        val scrollPane = JScrollPane(chatArea).apply {
            preferredSize = java.awt.Dimension(400, 600) // Set preferred size
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        }

        add(scrollPane)
        add(inputField)

        inputField.addActionListener { _ ->
            val message = inputField.text
            if (message.isNotBlank()) {
                addMessage("You: $message")
                inputField.text = ""
                handleUserMessage(message)
            }
        }
    }

    /**
     * Adds a message to the chat area.
     * @param message The message to display.
     */
    private fun addMessage(message: String) {
        chatArea.append("$message\n")
        chatArea.caretPosition = chatArea.text.length
        chatArea.repaint()
    }

    /**
     * Handles the user message by sending it to the AI model and displaying the response.
     * @param message The user message to send.
     */
    private fun handleUserMessage(message: String) {
        val userMessageJson = JSONObject().apply {
            put("role", "user")
            put("content", message)
        }
        messages.add(userMessageJson)

        getAIResponse { botResponse ->
            addMessage("Bot: $botResponse")
            val botMessageJson = JSONObject().apply {
                put("role", "assistant")
                put("content", botResponse)
            }
            messages.add(botMessageJson)
        }
    }

    /**
     * Sends the entire message history to the AI model and processes the response.
     * @param callback A function to handle the response from the AI model.
     */
    private fun getAIResponse(callback: (String) -> Unit) {
        val client = OkHttpClient()
        val url = "https://api.openai.com/v1/chat/completions"

        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray(messages))
            put("max_tokens", 500)
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
                        val botResponse = responseJson
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim()
                        callback(botResponse)
                    }
                } else {
                    callback("Server response error")
                }
            }
        })
    }

    /**
     * Loads the API key from the config.properties file.
     * @return The API key as a String.
     * @throws IllegalStateException if the config.properties file or the API key is not found.
     */
    private fun loadApiKey(): String {
        val properties = Properties()
        val inputStream = javaClass.getResourceAsStream("/config.properties")
            ?: throw IllegalStateException("Could not find config.properties file")
        inputStream.use { properties.load(it) }
        return properties.getProperty("apiKey")
            ?: throw IllegalStateException("API key not found in config.properties")
    }

    /**
     * Returns the content panel.
     * @return The JPanel containing the chat window.
     */
    fun getContent() = this
}
