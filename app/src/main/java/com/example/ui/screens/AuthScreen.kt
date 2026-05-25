package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassyCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLogin: (email: String, onSuccess: () -> Unit) -> Unit,
    onRegister: (email: String, username: String, balance: Double, currency: String, onSuccess: () -> Unit) -> Unit,
    loginError: String?,
    isLoading: Boolean,
    onAuthSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var isForgotPasswordMode by remember { mutableStateOf(false) }

    // Input States
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var initialBalance by remember { mutableStateOf("10000") }
    var selectedCurrency by remember { mutableStateOf("USD") }

    var localError by remember { mutableStateOf<String?>(null) }
    var successToast by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // Header icon
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Logo",
                tint = FintechBlue,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "K I N E T I X",
                color = TextSilverBase,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Text(
                text = "P R O  J O U R N A L",
                color = FintechBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 24.dp)
            )

            // Dynamic Form Mode Column
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                if (isForgotPasswordMode) {
                    // Password recovery view
                    Text(
                        "Recover Password",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Enter your account email to receive automatic password recovery link.",
                        color = TextMutedGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FintechBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextSilverBase,
                            unfocusedTextColor = TextSilverBase
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (email.isEmpty()) {
                                localError = "Please enter your email."
                            } else {
                                successToast = "A password recovery link has been sent to $email."
                                isForgotPasswordMode = false
                                localError = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Send Reset Link", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Back to Login",
                        color = FintechBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                isForgotPasswordMode = false
                                localError = null
                            }
                    )

                } else {
                    // Normal Login / Register view
                    Text(
                        text = if (isRegisterMode) "Create Trading Ledger" else "Access Trading Ledger",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (localError != null || loginError != null) {
                        val errMsg = localError ?: loginError
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BearishCrimson.copy(alpha = 0.15f))
                                .border(1.dp, BearishCrimson.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(errMsg ?: "", color = BearishCrimson, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSilverBase,
                            unfocusedTextColor = TextSilverBase,
                            focusedBorderColor = FintechBlue,
                            unfocusedBorderColor = GlassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    if (isRegisterMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = TextMutedGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSilverBase,
                                unfocusedTextColor = TextSilverBase,
                                focusedBorderColor = FintechBlue,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_username_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = initialBalance,
                            onValueChange = { initialBalance = it },
                            label = { Text("Initial Deposit Balance") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = TextMutedGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSilverBase,
                                unfocusedTextColor = TextSilverBase,
                                focusedBorderColor = FintechBlue,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_balance_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Currency: ", color = TextMutedGray, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            listOf("USD", "EUR", "BTC", "GBP").forEach { curr ->
                                val active = selectedCurrency == curr
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) FintechBlue else ObsidianCard)
                                        .clickable { selectedCurrency = curr }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        curr,
                                        color = if (active) Color.White else TextMutedGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (Optional)") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextMutedGray) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextMutedGray
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSilverBase,
                                unfocusedTextColor = TextSilverBase,
                                focusedBorderColor = FintechBlue,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = FintechBlue,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(32.dp)
                        )
                    } else {
                        Button(
                            onClick = {
                                localError = null
                                if (email.isEmpty() || !email.contains("@")) {
                                    localError = "Please enter a valid email address."
                                    return@Button
                                }
                                if (isRegisterMode) {
                                    if (username.isEmpty()) {
                                        localError = "Please enter a username."
                                        return@Button
                                    }
                                    val b = initialBalance.toDoubleOrNull() ?: 10000.0
                                    onRegister(email, username, b, selectedCurrency) {
                                        onAuthSuccess()
                                    }
                                } else {
                                    onLogin(email) {
                                        onAuthSuccess()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_auth_button")
                        ) {
                            Text(
                                text = if (isRegisterMode) "Register Account" else "Sign In",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isRegisterMode) "Login Instead" else "Create Account",
                            color = FintechBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable {
                                    isRegisterMode = !isRegisterMode
                                    localError = null
                                }.testTag("toggle_auth_mode_button")
                        )
                        if (!isRegisterMode) {
                            Text(
                                text = "Forgot Password?",
                                color = TextMutedGray,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clickable {
                                        isForgotPasswordMode = true
                                        localError = null
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sandbox Demo bypass for quick evaluations
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechPlusGlassThemeVariant)
                    .clickable {
                        localError = null
                        onRegister("demo@bulljournal.com", "DemoTrader", 25000.0, "USD") {
                            onAuthSuccess()
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = "Bypass", tint = WarningGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Quick Explore (Demo Trader Acc)",
                    color = WarningGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }

    // Custom overlay check-email toast dialog
    AnimatedVisibility(
        visible = successToast != null,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { successToast = null },
            contentAlignment = Alignment.Center
        ) {
            GlassyCard(modifier = Modifier.padding(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Sent",
                    tint = BullishEmerald,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Email Sent Successfully",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    successToast ?: "",
                    color = TextSilverBase,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { successToast = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            }
        }
    }
}

private val FintechPlusGlassThemeVariant = Color(0x1BFFC107)
