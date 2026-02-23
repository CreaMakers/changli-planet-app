package com.creamaker.changli_planet_app.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme

private object AuthLayout {
    val horizontal = 20.dp
    val topGap = 14.dp
    val sectionGap = 20.dp
    val cardRadius = 20.dp
    val cardPadding = 16.dp
    val fieldHeight = 60.dp
    val buttonHeight = 56.dp
}

@Composable
internal fun AuthScaffold(content: @Composable ColumnScope.() -> Unit) {
    val colors = AppTheme.colors
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colors.bgSecondaryColor.copy(alpha = 0.8f),
            colors.bgPrimaryColor
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .drawBehind {
                drawCircle(
                    color = colors.commonColor.copy(alpha = 0.09f),
                    radius = size.minDimension * 0.42f,
                    center = center.copy(x = size.width * 0.15f, y = size.height * 0.08f),
                    style = Fill
                )
                drawCircle(
                    color = colors.bgCardHighContrastColor.copy(alpha = 0.7f),
                    radius = size.minDimension * 0.5f,
                    center = center.copy(x = size.width * 0.95f, y = size.height * 0.02f),
                    style = Fill
                )
            }
    ) {
        val dynamicTopOffset = (maxHeight * 0.06f).coerceIn(18.dp, 42.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AuthLayout.horizontal, vertical = AuthLayout.topGap)
                .padding(top = dynamicTopOffset)
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter),
            content = content
        )
    }
}

@Composable
internal fun AuthTopBar(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    rightActionText: String? = null,
    onRightAction: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "返回",
                    tint = colors.primaryTextColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(36.dp))
        }

        if (!rightActionText.isNullOrBlank() && onRightAction != null) {
            Text(
                text = rightActionText,
                color = colors.textButtonColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(onClick = onRightAction)
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
internal fun AuthHero(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!badge.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(colors.bgCardHighContrastColor)
                    .border(
                        width = 1.dp,
                        color = colors.outlineLowContrastColor,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badge,
                    color = colors.functionalTextColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = title,
            color = colors.primaryTextColor,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = subtitle,
            color = colors.primaryTextColor.copy(alpha = 0.56f),
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
internal fun AuthSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
internal fun AuthInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = AppTheme.colors
    val trailingWithSafePadding: (@Composable () -> Unit)? = if (trailing == null) {
        null
    } else {
        {
            Box(modifier = Modifier.padding(end = 10.dp)) {
                trailing()
            }
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = "请输入$label",
                color = colors.secondaryTextColor.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        trailingIcon = trailingWithSafePadding,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.commonColor.copy(alpha = 0.22f),
            unfocusedBorderColor = colors.outlineLowContrastColor.copy(alpha = 0.28f),
            cursorColor = colors.commonColor,
            focusedTextColor = colors.primaryTextColor,
            unfocusedTextColor = colors.primaryTextColor,
            focusedContainerColor = colors.bgCardColor.copy(alpha = 0.94f),
            unfocusedContainerColor = colors.bgCardColor.copy(alpha = 0.88f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(AuthLayout.fieldHeight)
    )
}

@Composable
internal fun AuthCodeAction(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (enabled) colors.bgSecondaryColor else colors.bgCardHighContrastColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (enabled) colors.commonColor.copy(alpha = 0.35f) else colors.outlineLowContrastColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) colors.commonColor else colors.textButtonColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun AuthCaptchaRow(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    sendText: String,
    sendEnabled: Boolean,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "验证码"
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuthInput(
            value = value,
            onValueChange = onValueChange,
            label = label,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        AuthCodeAction(
            text = sendText,
            enabled = sendEnabled,
            onClick = onSendClick
        )
    }
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, colors.outlineLowContrastColor.copy(alpha = 0.32f)),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.bgButtonColor,
            contentColor = colors.textButtonColor,
            disabledContainerColor = colors.bgButtonLowlightColor,
            disabledContentColor = colors.disabledTextColor
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(AuthLayout.buttonHeight)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun AuthLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Text(
        text = text,
        color = colors.functionalTextColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
internal fun AuthLinkRow(
    links: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier,
    arrangement: Arrangement.Horizontal = Arrangement.SpaceBetween
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        links.forEachIndexed { index, item ->
            AuthLink(text = item.first, onClick = item.second)
            if (index != links.lastIndex) {
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Composable
internal fun AuthAgreement(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = colors.commonColor,
                uncheckedColor = colors.secondaryTextColor
            ),
            modifier = Modifier.offset(x = (-11).dp)
        )
        Text(
            "我已阅读并同意",
            color = colors.secondaryTextColor,
            fontSize = 14.sp,
            modifier = Modifier.offset(x = (-10).dp)
        )
        AuthLink(
            text = "《长理星球用户协议》",
            onClick = {},
            modifier = Modifier.offset(x = (-10).dp)
        )
    }
}

@Composable
internal fun AuthSpacer(height: Dp = AuthLayout.sectionGap) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
internal fun AuthTwoLinks(
    leftText: String,
    rightText: String,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthLinkRow(
        links = listOf(leftText to onLeft, rightText to onRight),
        modifier = modifier,
        arrangement = Arrangement.SpaceBetween
    )
}

@Composable
private fun AuthPreviewContainer(content: @Composable ColumnScope.() -> Unit) {
    AppSkinTheme {
        AuthScaffold {
            content()
        }
    }
}

@Preview(showBackground = true, name = "AuthHero")
@Composable
private fun AuthHeroPreview() {
    AuthPreviewContainer {
        AuthTopBar(onBack = {}, rightActionText = "账号说明", onRightAction = {})
        AuthSpacer(36.dp)
        AuthHero(
            title = "登录",
            subtitle = "欢迎回来，继续你的长理星球"
        )
    }
}

@Preview(showBackground = true, name = "AuthSection")
@Composable
private fun AuthSectionPreview() {
    AuthPreviewContainer {
        AuthSection {
            AuthInput(value = TextFieldValue("changli"), onValueChange = {}, label = "账号")
            AuthInput(value = TextFieldValue("demo@changli.app"), onValueChange = {}, label = "密码")
            AuthAgreement(checked = true, onCheckedChange = {})
        }
    }
}

@Preview(showBackground = true, name = "AuthCaptchaRow")
@Composable
private fun AuthCaptchaRowPreview() {
    AuthPreviewContainer {
        AuthSection {
            AuthInput(value = TextFieldValue("demo@changli.app"), onValueChange = {}, label = "邮箱")
            AuthCaptchaRow(
                value = TextFieldValue("1234"),
                onValueChange = {},
                sendText = "获取验证码",
                sendEnabled = true,
                onSendClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "AuthActions")
@Composable
private fun AuthActionsPreview() {
    AuthPreviewContainer {
        AuthPrimaryButton(text = "登录", enabled = true, onClick = {})
        AuthSpacer(12.dp)
        AuthTwoLinks(
            leftText = "忘记密码",
            rightText = "邮箱登录",
            onLeft = {},
            onRight = {}
        )
    }
}
