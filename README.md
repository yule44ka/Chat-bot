# Chat Tool Window Plugin

This is a Kotlin-based IntelliJ IDEA plugin that provides a chat tool window to interact with
OpenAI's GPT-3.5-turbo model. The plugin creates a simple user interface to send and receive
messages from the AI model, allowing users to ask questions and receive responses within the IDE.

## Features

- **Chat Interface**: Includes a chat area to display conversation history and an input field to
  type messages.
- **System Message Configuration**: Initializes each conversation with a system message to guide the
  AI’s responses.
- **Scrollable Chat History**: Supports scrolling for larger conversations.

## Setup

### Prerequisites

1. [IntelliJ IDEA](https://www.jetbrains.com/idea/) installed.
2. An [OpenAI API key](https://platform.openai.com/account/api-keys) for accessing the GPT-3.5-turbo
   model.
3. Kotlin and Java set up within IntelliJ.

### Installation

1. Clone the repository
2. Open the project in IntelliJ IDEA.

3. Add the `config.properties` file in the `chatBot\src\main\resources` directory and include your
   OpenAI API key:
    ```
    apiKey=your_openai_api_key
    ```

### Usage

1. Run the plugin in a new IntelliJ instance.
2. Open the **Chat Tool Window** from the tool window sidebar.
3. Type your questions in the input field and press Enter to send. The AI model will respond in the
   chat area.

### Code Structure

- **ChatToolWindow**: Implements the chat panel UI and handles interactions with the OpenAI API.
- **ChatToolWindowFactory**: Sets up the tool window content within IntelliJ IDEA.

### Key Components

- **`ChatToolWindow`**:
    - **addMessage**: Appends messages to the chat area.
    - **handleUserMessage**: Processes the user input and retrieves AI responses.
    - **getAIResponse**: Sends a POST request to the OpenAI API and handles the response.
    - **loadApiKey**: Loads the API key from `config.properties`.

- **`ChatToolWindowFactory`**:
    - Integrates the chat panel into the IntelliJ tool window.

### Dependencies

- **OkHttp**: Used for making HTTP requests to the OpenAI API.
- **org.json**: Parses JSON responses from the API.
