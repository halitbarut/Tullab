package com.barutdev.tullab.ui.screens.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.util.tullabStringResource

@Composable
fun LegalConsentSection(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    val privacyLabel = tullabStringResource(id = R.string.settings_privacy_policy_label)
    val termsLabel = tullabStringResource(id = R.string.settings_terms_label)
    val consentText = tullabStringResource(id = R.string.onboarding_consent_template, privacyLabel, termsLabel)

    val primaryColor = MaterialTheme.colorScheme.primary
    val annotated: AnnotatedString = remember(consentText, primaryColor) {
        buildAnnotatedString {
            val privacyUrl = "https://gist.github.com/halitbarut/b6b011b0d3cca23bd36781b9465a3cef"
            val termsUrl = "https://gist.github.com/halitbarut/5a56f975637a6e815feaea41539854a2"
            val privacyStart = consentText.indexOf(privacyLabel)
            val termsStart = consentText.indexOf(termsLabel)

            append(consentText)
            if (privacyStart >= 0) {
                addStyle(SpanStyle(color = primaryColor), privacyStart, privacyStart + privacyLabel.length)
                addStringAnnotation(tag = "link", annotation = privacyUrl, start = privacyStart, end = privacyStart + privacyLabel.length)
            }
            if (termsStart >= 0) {
                addStyle(SpanStyle(color = primaryColor), termsStart, termsStart + termsLabel.length)
                addStringAnnotation(tag = "link", annotation = termsUrl, start = termsStart, end = termsStart + termsLabel.length)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        ClickableText(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
            onClick = { offset ->
                annotated.getStringAnnotations(tag = "link", start = offset, end = offset)
                    .firstOrNull()?.let { ann -> uriHandler.openUri(ann.item) }
            },
            modifier = Modifier.weight(1f)
        )
    }
}
