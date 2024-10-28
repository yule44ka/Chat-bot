import javax.swing.*
import java.awt.BorderLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Properties

class ChatToolWindow : JPanel(BorderLayout()) {
    private val chatArea = JTextArea().apply {
        isEditable = false
        lineWrap = true // Позволяет тексту переноситься
        wrapStyleWord = true // Переносит текст по словам
    }
    private val inputField = JTextField()
    private val apiKey = loadApiKey()
    private val messages = mutableListOf<JSONObject>() // Хранит историю сообщений

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        // Добавление JScrollPane для области чата
        val scrollPane = JScrollPane(chatArea).apply {
            preferredSize = java.awt.Dimension(400, 600) // Установка предпочтительного размера
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED // Прокрутка по необходимости
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

    private fun addMessage(message: String) {
        chatArea.append("$message\n")
        chatArea.caretPosition = chatArea.text.length // Перемещает каретку в конец текста
        chatArea.repaint() // Перерисовывает область чата
    }

    private fun handleUserMessage(message: String) {
        val userMessageJson = JSONObject().apply {
            put("role", "user")
            put("content", message)
        }
        messages.add(userMessageJson) // Добавляем сообщение пользователя в историю

        getAIResponse { botResponse ->
            addMessage("Bot: $botResponse")
            val botMessageJson = JSONObject().apply {
                put("role", "assistant")
                put("content", botResponse)
            }
            messages.add(botMessageJson) // Добавляем ответ бота в историю
        }
    }

    private fun getAIResponse(callback: (String) -> Unit) {
        val client = OkHttpClient()
        val url = "https://api.openai.com/v1/chat/completions"

        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray(messages)) // Отправляем всю историю сообщений
            put("max_tokens", 500)
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Ошибка: ${e.message}")
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
                    callback("Ошибка ответа от сервера")
                }
            }
        })
    }

    private fun loadApiKey(): String {
        val properties = Properties()
        val inputStream = javaClass.getResourceAsStream("/config.properties")
            ?: throw IllegalStateException("Не удалось найти файл config.properties")
        inputStream.use { properties.load(it) }
        return properties.getProperty("apiKey")
            ?: throw IllegalStateException("API ключ не найден в файле config.properties")
    }

    fun getContent() = this
}
