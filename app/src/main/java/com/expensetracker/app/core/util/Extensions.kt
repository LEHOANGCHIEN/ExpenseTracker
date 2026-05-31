package com.expensetracker.app.core.util

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// ---- Modifier extensions ----

fun Modifier.shimmerEffect(
    baseColor: Color = Color(0xFFE0E0E0),
    highlightColor: Color = Color(0xFFF5F5F5),
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )
    background(
        Brush.linearGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.7f),
                highlightColor.copy(alpha = 0.9f),
                baseColor.copy(alpha = 0.7f),
            ),
            start = Offset(translateAnim - 300f, translateAnim - 300f),
            end = Offset(translateAnim, translateAnim),
        )
    )
}

fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier =
    if (condition) this.then(modifier(Modifier)) else this

fun Modifier.paddingHorizontal(horizontal: Dp = 16.dp): Modifier =
    this.padding(horizontal = horizontal)

fun Modifier.paddingVertical(vertical: Dp = 16.dp): Modifier =
    this.padding(vertical = vertical)

// ---- Flow extensions ----

fun <T, R> Flow<T>.mapResult(transform: (T) -> R): Flow<Result<R>> =
    map { Result.success(transform(it)) }.catch { emit(Result.failure(it)) }

fun <T> Flow<T>.catchAndReturn(default: T): Flow<T> =
    catch { emit(default) }
