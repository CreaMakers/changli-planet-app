package com.zhelearn.CSUSTPlanet.im.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.core.theme.AppSkinTheme
import com.zhelearn.CSUSTPlanet.core.theme.AppTheme

@Composable
fun UnderConstructionScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.bgPrimaryColor)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.under_construction),
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun UnderConstructionScreenPreview() {
    AppSkinTheme {
        UnderConstructionScreen()
    }
}
