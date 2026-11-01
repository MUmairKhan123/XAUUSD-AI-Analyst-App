package com.xauusd.analyst.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xauusd.analyst.data.Decision
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun decisionColor(d: Decision?): Color = when (d) {
    Decision.BUY -> BuyGreen
    Decision.SELL -> SellRed
    else -> WaitGrey
}

fun formatDate(ts: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(ts))

@Composable
fun XCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Outline,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
fun Title(text: String) {
    Text(text, color = Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
}

@Composable
fun Muted(text: String, modifier: Modifier = Modifier) {
    Text(text, color = TextMuted, fontSize = 13.sp, lineHeight = 18.sp, modifier = modifier)
}

@Composable
fun Body(text: String, modifier: Modifier = Modifier) {
    Text(text, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp, modifier = modifier)
}

@Composable
fun DecisionBadge(d: Decision?) {
    val c = decisionColor(d)
    Text(
        text = d?.name ?: "NOT SET",
        color = c,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        modifier = Modifier
            .background(c.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
fun Chip(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun GoldButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold, contentColor = Navy,
            disabledContainerColor = NavyLight, disabledContentColor = TextMuted
        )
    ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
}

@Composable
fun OutlineButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Gold,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (enabled) color else Outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color, disabledContentColor = TextMuted),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) }
}

@Composable
fun WarningBox(text: String, color: Color = Gold) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(text, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
fun RowScope.Stat(label: String, value: String, valueColor: Color = TextPrimary) {
    Column(modifier = Modifier.weight(1f)) {
        Muted(label)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun LevelRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Muted(label)
        Text(
            value.ifBlank { "-" },
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun XField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    number: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = if (number) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = Outline,
            focusedLabelColor = Gold,
            unfocusedLabelColor = TextMuted,
            cursorColor = Gold,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
fun EmptyState(title: String, text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Muted(text)
    }
}
