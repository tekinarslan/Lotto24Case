package de.lotto24case.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Aura Lotto Design System Typography Tokens.
 */
object AuraTypography {
    val displayLg = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 48.sp,
        letterSpacing = (-0.02).sp
    )

    val headlineMd = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).sp
    )

    val headlineSm = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    )

    val bodyMd = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp
    )

    val bodySm = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp
    )

    val labelLg = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        letterSpacing = 0.01.sp
    )

    val labelMd = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp,
        letterSpacing = 0.02.sp
    )

    val labelSm = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 14.sp,
        letterSpacing = 0.05.sp
    )

    val lotteryNumber = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 22.sp,
        letterSpacing = (-0.01).sp
    )

    val lotteryNumberSm = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 16.sp
    )

    val materialTypography = Typography(
        headlineMedium = headlineMd,
        headlineSmall = headlineSm,
        bodyMedium = bodyMd,
        bodySmall = bodySm,
        labelLarge = labelLg,
        labelMedium = labelMd,
        labelSmall = labelSm
    )
}
