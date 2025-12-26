package com.tajir.sarf.ui.theme

import androidx.compose.ui.graphics.Color

// Final UI palette (light-only): clean white base + soft green accent.
val SarfGreen = Color(0xFF3FAF7F)
val SarfGreenStrong = Color(0xFF2E9E6E)
val SarfGreenSoft = Color(0xFFECF8F2)
// Secondary accent that pairs well with green for outlines (cool blue‑gray).
val SarfLineAlt = Color(0xFF94A3B8) // slate-400

// Accent (calculator special keys)
val SarfOrange = Color(0xFFF2994A)
val SarfOrangeStrong = Color(0xFFEF6C00)

// Light UI: pure white background (user preference).
val SarfBg = Color(0xFFFFFFFF)
val SarfSurface = Color(0xFFFFFFFF)
val SarfText = Color(0xFF0F172A)        // slate-900
val SarfTextMuted = Color(0xFF64748B)   // slate-500/600
// User preference: ALL "lines" (outlines/borders) should be green.
val SarfOutline = SarfGreen.copy(alpha = 0.75f)

// Dark theme palette
val SarfBgDark = Color(0xFF0F1110)
val SarfSurfaceDark = Color(0xFF121614)
val SarfTextDark = Color(0xFFEDEDED)
val SarfOutlineDark = Color(0xFF2A2F2C)