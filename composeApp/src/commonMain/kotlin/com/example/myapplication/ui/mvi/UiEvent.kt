package com.example.myapplication.ui.mvi

/**
 * 標記介面，所有 UI Event 都應實作此介面
 * 
 * Event 是單次性的，不應該保存在 State 中
 * Event 用於導航、顯示 Toast、顯示 Dialog 等單次操作
 * Event 應該是 sealed class，確保類型安全
 */
interface UiEvent
