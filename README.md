# SMMWE Level Editor

SMMWE 关卡编辑器，提供独立的浏览器版与原生 Android 手机/平板版（最低 Android 5.0 / API 21）。

## Android 版

Android 版使用 Java 原生界面、原生 Canvas 绘制关卡网格，并通过 Android Storage Access Framework 打开和保存文件；不使用 WebView，也不加载网页资源。SWE 容器的 Base64 与 HMAC-SHA1 校验/写入在 Android 原生代码中完成。

- 原生触摸画布与网格绘制
- 新建关卡、图块绘制与擦除
- 导入 SWE 并校验文件完整性
- SWE 数据层浏览与记录编辑（S2/S3/S4/S5/S8）
- 导出带重新计算校验值的 SWE 文件
- 保存原生工程文件（.smmweproj）

## 浏览器版

- `web/`：独立浏览器编辑器。
- `core/`：浏览器版项目模型、历史记录、SWE 解析与写入。
- `tests/`：SWE 校验和往返测试。

## 构建 Android

使用 Android Studio 打开 `android/`，最低 SDK 为 21。GitHub Actions 手动运行 **Android APK**，构建原生 Debug APK 并上传 artifact。

## SWE 兼容性

格式适配依据已知样本 `JituiLeet/Test/YINBI.swe`。导出文件仍建议在目标 SMMWE 游戏版本中实际导入验证。详见 [SWE compatibility notes](docs/SWE-COMPATIBILITY.md)。
