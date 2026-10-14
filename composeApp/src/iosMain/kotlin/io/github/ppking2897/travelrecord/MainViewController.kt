package com.example.myapplication

import androidx.compose.ui.window.ComposeUIViewController
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun MainViewController() = ComposeUIViewController { App() }
