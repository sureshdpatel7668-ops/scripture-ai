package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun parseMarkdownToAnnotatedString(text: String, baseColor: Color = Color.Unspecified): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold-Italic ***text*** or **text**
                text.startsWith("***", i) -> {
                    val end = text.indexOf("***", i + 3)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
                        append(text.substring(i + 3, end))
                        pop()
                        i = end + 3
                    } else {
                        append("***")
                        i += 3
                    }
                }
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(text.substring(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append("**")
                        i += 2
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        append("*")
                        i += 1
                    }
                }
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.2f)))
                        append(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        append("`")
                        i += 1
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Blockquote(val text: String) : MarkdownBlock()
    object Spacer : MarkdownBlock()
    object Divider : MarkdownBlock()
    data class Heading(val text: String, val level: Int) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class OrderedItem(val number: String, val text: String) : MarkdownBlock()
}

fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val lines = text.split("\n")
    val blocks = mutableListOf<MarkdownBlock>()
    val currentBlockquoteLines = mutableListOf<String>()

    fun flushBlockquote() {
        if (currentBlockquoteLines.isNotEmpty()) {
            blocks.add(MarkdownBlock.Blockquote(currentBlockquoteLines.joinToString("\n")))
            currentBlockquoteLines.clear()
        }
    }

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith(">")) {
            currentBlockquoteLines.add(trimmed.removePrefix(">").trim())
            continue
        } else {
            flushBlockquote()
        }

        when {
            trimmed.isEmpty() -> {
                blocks.add(MarkdownBlock.Spacer)
            }
            trimmed == "---" || trimmed == "===" || trimmed == "***" -> {
                blocks.add(MarkdownBlock.Divider)
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Heading(trimmed.removePrefix("# ").trim(), 1))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Heading(trimmed.removePrefix("## ").trim(), 2))
            }
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Heading(trimmed.removePrefix("### ").trim(), 3))
            }
            trimmed.startsWith("#### ") -> {
                blocks.add(MarkdownBlock.Heading(trimmed.removePrefix("#### ").trim(), 4))
            }
            trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2).trim()))
            }
            trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                val dotIdx = trimmed.indexOf(".")
                val number = trimmed.substring(0, dotIdx + 1)
                val content = trimmed.substring(dotIdx + 1).trim()
                blocks.add(MarkdownBlock.OrderedItem(number, content))
            }
            else -> {
                blocks.add(MarkdownBlock.Paragraph(line))
            }
        }
    }
    flushBlockquote()
    return blocks
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.onSurface,
    fontSizeMultiplier: Float = 1f
) {
    val scaleBody = MaterialTheme.typography.bodyMedium.copy(
        fontSize = (14.sp.value * fontSizeMultiplier).sp,
        lineHeight = (20.sp.value * fontSizeMultiplier).sp
    )
    val scaleBodySmall = MaterialTheme.typography.bodySmall.copy(
        fontSize = (12.sp.value * fontSizeMultiplier).sp,
        lineHeight = (16.sp.value * fontSizeMultiplier).sp
    )
    val scaleBodyLarge = MaterialTheme.typography.bodyLarge.copy(
        fontSize = (16.sp.value * fontSizeMultiplier).sp,
        lineHeight = (22.sp.value * fontSizeMultiplier).sp
    )
    val scaleTitleLarge = MaterialTheme.typography.titleLarge.copy(
        fontSize = (22.sp.value * fontSizeMultiplier).sp,
        lineHeight = (28.sp.value * fontSizeMultiplier).sp
    )
    val scaleTitleMedium = MaterialTheme.typography.titleMedium.copy(
        fontSize = (18.sp.value * fontSizeMultiplier).sp,
        lineHeight = (24.sp.value * fontSizeMultiplier).sp
    )

    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Spacer -> {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                is MarkdownBlock.Divider -> {
                    Divider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                }
                is MarkdownBlock.Heading -> {
                    val style = when (block.level) {
                        1 -> scaleTitleLarge.copy(fontWeight = FontWeight.Bold)
                        2 -> scaleTitleMedium.copy(fontWeight = FontWeight.Bold)
                        3 -> scaleBodyLarge.copy(fontWeight = FontWeight.Bold)
                        else -> scaleBody.copy(fontWeight = FontWeight.Bold)
                    }
                    val color = when (block.level) {
                        1, 2 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                    Text(
                        text = parseMarkdownToAnnotatedString(block.text, baseColor),
                        style = style,
                        color = color,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = scaleBody,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseMarkdownToAnnotatedString(block.text, baseColor),
                            style = scaleBody,
                            color = baseColor
                        )
                    }
                }
                is MarkdownBlock.OrderedItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = block.number,
                            style = scaleBody.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseMarkdownToAnnotatedString(block.text, baseColor),
                            style = scaleBody,
                            color = baseColor
                        )
                    }
                }
                is MarkdownBlock.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(IntrinsicSize.Min)
                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseMarkdownToAnnotatedString(block.text, baseColor),
                            style = scaleBody.copy(fontStyle = FontStyle.Italic),
                            color = baseColor.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, end = 4.dp)
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseMarkdownToAnnotatedString(block.text, baseColor),
                        style = scaleBody,
                        color = baseColor
                    )
                }
            }
        }
    }
}
