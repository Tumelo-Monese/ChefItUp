package com.chefitup.app.presentation.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chefitup.app.R
import com.chefitup.app.presentation.components.ChefItUpButton
import com.chefitup.app.presentation.components.ChefItUpPasswordField
import com.chefitup.app.presentation.components.ChefItUpTextButton
import com.chefitup.app.presentation.components.ChefItUpTextField
import com.chefitup.app.presentation.components.PasswordStrengthIndicator
import com.chefitup.app.presentation.theme.CreamCanvas
import com.chefitup.app.presentation.theme.WarmStone
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                RegisterEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(CreamCanvas, WarmStone, CreamCanvas)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            ChefItUpTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = stringResource(R.string.register_name),
                errorMessage = uiState.nameError?.let { stringResource(it) },
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(12.dp))

            ChefItUpTextField(
                value = uiState.surname,
                onValueChange = viewModel::onSurnameChange,
                label = stringResource(R.string.register_surname),
                errorMessage = uiState.surnameError?.let { stringResource(it) },
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(12.dp))

            ChefItUpTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(R.string.auth_email),
                errorMessage = uiState.emailError?.let { stringResource(it) },
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(12.dp))

            ChefItUpTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                label = stringResource(R.string.register_phone),
                errorMessage = uiState.phoneError?.let { stringResource(it) },
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(12.dp))

            ChefItUpTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                label = stringResource(R.string.register_username),
                errorMessage = uiState.usernameError?.let { stringResource(it) },
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(12.dp))

            ChefItUpPasswordField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = stringResource(R.string.auth_password),
                passwordVisible = uiState.passwordVisible,
                onToggleVisibility = viewModel::togglePasswordVisibility,
                errorMessage = uiState.passwordError?.let { stringResource(it) },
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordStrengthIndicator(strength = uiState.passwordStrength)
            Spacer(modifier = Modifier.height(12.dp))

            ChefItUpPasswordField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = stringResource(R.string.auth_confirm_password),
                passwordVisible = uiState.confirmPasswordVisible,
                onToggleVisibility = viewModel::toggleConfirmPasswordVisibility,
                errorMessage = uiState.confirmPasswordError?.let { stringResource(it) },
                imeAction = ImeAction.Done,
                onImeAction = viewModel::register
            )

            uiState.formError?.let { errorRes ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(errorRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ChefItUpButton(
                text = stringResource(R.string.register_button),
                onClick = viewModel::register,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.register_have_account),
                    style = MaterialTheme.typography.bodyMedium
                )
                ChefItUpTextButton(
                    text = stringResource(R.string.register_sign_in),
                    onClick = onNavigateToLogin
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
