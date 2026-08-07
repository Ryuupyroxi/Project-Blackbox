package com.blackbox.ai.ui.theme

import androidx.compose.ui.graphics.Color

// ===== Cyber-futuristic palette =====
// Deep near-black background with neon cyan primary, magenta secondary and
// electric violet tertiary accents. Light (day) scheme reads the same identity
// with adjusted contrast on light surfaces.

// Dark scheme
val CyberBackgroundDark = Color(0xFF05070C)
val CyberSurfaceDark = Color(0xFF0B0E16)
val CyberSurfaceVariantDark = Color(0xFF131A29)
val CyberOnSurfaceDark = Color(0xFFDCE7F5)
val CyberOnSurfaceVariantDark = Color(0xFF9AA7BC)

val NeonCyan = Color(0xFF00E5FF)
val NeonCyanDim = Color(0xFF0096AD)
val OnNeonCyanDark = Color(0xFF001F25)

val NeonMagenta = Color(0xFFFF2D9F)
val NeonMagentaDim = Color(0xFFB3006E)
val OnNeonMagentaDark = Color(0xFF370019)

val ElectricViolet = Color(0xFF8B7BFF)
val ElectricVioletDim = Color(0xFF5B49D6)
val OnElectricVioletDark = Color(0xFF15004D)

val CyberSuccess = Color(0xFF00E676)
val CyberWarning = Color(0xFFFFEA00)
val CyberError = Color(0xFFFF3D6E)
val OnErrorDark = Color(0xFF2D0000)

// Light scheme
val CyberBackgroundLight = Color(0xFFEEF4FF)
val CyberSurfaceLight = Color(0xFFF7FAFF)
val CyberSurfaceVariantLight = Color(0xFFE2E8F5)
val CyberOnSurfaceLight = Color(0xFF101623)
val CyberOnSurfaceVariantLight = Color(0xFF45506A)

val OnNeonCyanLight = Color(0xFFFFFFFF)
val OnNeonMagentaLight = Color(0xFFFFFFFF)
val OnElectricVioletLight = Color(0xFFFFFFFF)
val OnErrorLight = Color(0xFFFFFFFF)

// Readable dark text for the bright neon *containers* in light mode.
val OnBrightContainerLight = Color(0xFF001318)