package com.quickime.android.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickime.core.cs.CSSuggestion

@Composable
fun QuickImeView(
    context: android.content.Context,
    onKeyListener: OnKeyPressed,
    onSuggestionListener: OnSuggestionListener
) {
    var currentCode by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(emptyList<CSSuggestion>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5)
            .padding(4.dp)
    ) {
        CandidateBar(
            suggestions = suggestions,
            onSelected = onSuggestionListener
        )
        Spacer(modifier = Modifier.height(4.dp))
        KeyboardLayout(
            onPressed = { key ->
                onKeyListener(key)
                if (key.type == KeyType.Character) {
                    currentCode = currentCode + key.char.toString()
                }
            }
        )
    }
}

@Composable
private fun CandidateBar(
    suggestions: List<CSSuggestion>,
    onSelected: OnSuggestionListener
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        itemsIndexed(suggestions) { index, suggestion ->
            CandidateItem(
                index = index + 1,
                text = suggestion.text,
                source = suggestion.source.name,
                onClick = { onSelected(index) }
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
private fun CandidateItem(
    index: Int,
    text: String,
    source: String,
    onClick: () -> Unit
) {
    val tagColor = when (source) {
        "KnowledgeBase" -> Color(0xFF4CAF50)
        "AIGenerated" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
    Surface(
        modifier = Modifier
            .background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$index",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text.take(20),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when (source) {
                    "KnowledgeBase" -> "[知]"
                    "AIGenerated" -> "[AI]"
                    else -> ""
                },
                fontSize = 10.sp,
                color = tagColor
            )
        }
    }
}

@Composable
private fun KeyboardLayout(
    onPressed: (KeyEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyboardRow(
            keys = listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'),
            onPressed = onPressed
        )
        KeyboardRow(
            keys = listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'),
            onPressed = onPressed
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M').forEach { char ->
                KeyButton(
                    char = char,
                    modifier = Modifier.weight(1f),
                    onClick = { onPressed(KeyEvent(KeyType.Character, char) }
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            KeyButton(
                char = '⌫',
                modifier = Modifier.weight(1f),
                onClick = { onPressed(KeyEvent(KeyType.Backspace) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyButton(
                text = "空格",
                modifier = Modifier.weight(3f),
                onClick = { onPressed(KeyEvent(KeyType.Space) }
            )
            KeyButton(
                text = "🌐",
                modifier = Modifier.weight(1f),
                onClick = { onPressed(KeyEvent(KeyType.SwitchKeyboard) }
            )
        }
    }
}

@Composable
private fun KeyboardRow(
    keys: List<Char>,
    onPressed: (KeyEvent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        keys.forEach { char ->
            KeyButton(
                char = char,
                modifier = Modifier.weight(1f),
                onClick = { onPressed(KeyEvent(KeyType.Character, char) }
            )
        }
    }
}

@Composable
private fun KeyButton(
    char: Char? = null,
    text: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text ?: char?.toString() ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
