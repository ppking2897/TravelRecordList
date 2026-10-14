package com.example.myapplication

import androidx.compose.ui.ExperimentalComposeUiApi

import androidx.compose.ui.window.ComposeViewport

import com.example.myapplication.di.initKoin

import kotlin.time.ExperimentalTime



@ExperimentalTime

@OptIn(ExperimentalComposeUiApi::class)

fun main() {

    initKoin()

    ComposeViewport {

        App()

    }

}
