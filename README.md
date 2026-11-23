# SMMWE Level Editor

SMMWE 关卡编辑器，提供浏览器版与独立原生 Android 手机/平板版（最低 Android 5.0 / API 21）。

## Android 版界面

原生 Android Canvas 编辑界面采用横向工具栏、左侧关卡面板、中央网格画布和右侧快捷操作栏的布局，视觉参考用户提供的关卡编辑器截图。手机竖屏会自动压缩侧栏与网格区域。

像素美术素材使用 Kenney Pixel Platformer（CC0 1.0），由 GitHub Actions 构建时从素材仓库获取并打包，不包含 Nintendo / Mario 原版美术资源。素材来源： https://www.kenney.nl/assets/pixel-platformer

## Android 功能

- 原生触摸画布、缩放和平移
- 网格绘制与擦除
- SWE 文件导入、HMAC-SHA1 校验、数据层编辑和导出
- 原生工程文件保存与打开
- 最低 Android 5.0 / API 21

## 构建

使用 Android Studio 打开 `android/`。GitHub Actions 手动运行 **Android APK**，自动获取 CC0 像素美术素材并构建原生 APK。

SWE 格式适配依据已知样本；导出文件仍建议在目标 SMMWE 游戏版本中实际导入验证。
