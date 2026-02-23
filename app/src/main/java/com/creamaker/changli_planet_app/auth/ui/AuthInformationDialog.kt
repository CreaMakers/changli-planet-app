package com.creamaker.changli_planet_app.auth.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme

@Composable
internal fun AuthInformationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = colors.primaryTextColor,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = message,
                color = colors.secondaryTextColor
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "我知道了",
                    color = colors.commonColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = colors.bgCardColor
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthInformationDialogPreview() {
    AppSkinTheme {
        AuthInformationDialog(
            title = "登录失败",
            message = "账号或密码错误",
            onDismiss = {}
        )
    }
}
