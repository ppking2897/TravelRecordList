# 產品概述 (Product Overview)

這是一個使用 Kotlin Multiplatform 和 Compose Multiplatform 建置的**旅遊流程記事應用程式** (Travel Itinerary Management Application)。

## 核心功能 (Core Features)

- 建立和管理旅遊行程，包含日期、描述和項目
- 新增詳細的行程項目（地點、活動、筆記、照片）
- 依日期分組和篩選項目
- 從行程生成路線
- 查看旅遊歷史
- 跨平台支援：Android、iOS 和 Web (JS/Wasm)

## 架構 (Architecture)

應用程式遵循 **Clean Architecture** 原則，具有清晰的分層：
- **Data Layer**：Repositories、storage services、models
- **Domain Layer**：Use cases 處理業務邏輯
- **UI Layer**：Compose screens、ViewModels、navigation

## 關鍵技術 (Key Technologies)

- Kotlin Multiplatform 用於共享業務邏輯
- Compose Multiplatform 用於共享 UI
- Koin 用於依賴注入
- kotlinx.datetime 用於日期處理
- kotlinx.serialization 用於資料持久化
