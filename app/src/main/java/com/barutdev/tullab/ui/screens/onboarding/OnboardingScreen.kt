package com.barutdev.tullab.ui.screens.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onCompleted: () -> Unit
) {
    val consentChecked by viewModel.consentChecked.collectAsState()
    
    // Animation states
    val heroAlpha = remember { Animatable(0f) }
    val heroTranslation = remember { Animatable(20f) }
    
    val cardsAlpha = remember { Animatable(0f) }
    val cardsTranslation = remember { Animatable(20f) }
    
    val actionAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // Staggered entrance animations
        heroAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        heroTranslation.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        
        delay(100)
        
        cardsAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        cardsTranslation.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        
        delay(100)
        
        actionAlpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp, bottom = 24.dp)
            .testTag("OnboardingScreen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WelcomeHeroSection(
                modifier = Modifier.graphicsLayer {
                    alpha = heroAlpha.value
                    translationY = heroTranslation.value
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ValuePillList(
                modifier = Modifier.graphicsLayer {
                    alpha = cardsAlpha.value
                    translationY = cardsTranslation.value
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = actionAlpha.value
                }
        ) {
            LegalConsentSection(
                checked = consentChecked,
                onCheckedChange = viewModel::onConsentCheckedChange,
                modifier = Modifier.testTag("ConsentCheckbox")
            )
            
            GetStartedAction(
                isEnabled = consentChecked,
                onClick = { viewModel.completeOnboarding(onCompleted) },
                modifier = Modifier.testTag("GetStartedButton")
            )
        }
    }
}
