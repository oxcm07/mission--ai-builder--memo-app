package app.util

import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path

actual fun openNotesDataFolder(): Boolean {
    val folder = Path.of(System.getProperty("user.home"), ".memo")
    Files.createDirectories(folder)

    if (!Desktop.isDesktopSupported()) return false

    val desktop = Desktop.getDesktop()
    if (!desktop.isSupported(Desktop.Action.OPEN)) return false

    desktop.open(folder.toFile())
    return true
}
