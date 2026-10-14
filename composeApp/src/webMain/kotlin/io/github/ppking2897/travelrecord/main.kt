package io.github.ppking2897.travelrecord

import androidx.compose.ui.ExperimentalComposeUiApi

import androidx.compose.ui.window.ComposeViewport

import io.github.ppking2897.travelrecord.di.initKoin

import kotlin.time.ExperimentalTime



@ExperimentalTime

@OptIn(ExperimentalComposeUiApi::class)

fun main() {

    initKoin()

    ComposeViewport {

        App()

    }

}
