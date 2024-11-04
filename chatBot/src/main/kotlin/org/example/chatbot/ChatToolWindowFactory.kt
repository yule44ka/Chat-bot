package org.example.chatbot

import ChatToolWindow
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * Factory class to create and display the ChatToolWindow within IntelliJ's tool window.
 * Implements ToolWindowFactory and DumbAware to allow the window to function in various IDE states.
 */
class ChatToolWindowFactory : ToolWindowFactory, DumbAware {

    /**
     * Creates the tool window content by initializing ChatToolWindow and adding it to the provided ToolWindow.
     * @param project The current IntelliJ project context.
     * @param toolWindow The tool window where the chat content will be displayed.
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatToolWindow = ChatToolWindow()
        val content = toolWindow.contentManager.factory.createContent(chatToolWindow.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }
}
