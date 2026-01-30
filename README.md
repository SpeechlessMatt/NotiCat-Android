# 😼 NotiCat for Android

[![Release](https://img.shields.io/github/v/release/YOURNAME/noticat-android?logo=github)](https://github.com/SpeechlessMatt/NotiCat-Android/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?logo=kotlin)]()
[![Download](https://img.shields.io/badge/Download-APK-brightgreen)](https://github.com/YOURNAME/noticat-android/releases/latest)

> **你的信息聚合遥控器**  
> 一个客户端，管理多个 NotiCat 服务器。支持多账号、多节点，让通知管理像切歌一样简单。

---

## 📲 下载安装

**不需要编译，不需要配置环境，下载即用：**

1. 进入 [**Releases**](https://github.com/SpeechlessMatt/NotiCat-Android/releases/latest) 页面
2. 下载 `app-release.apk`（约 15MB）
3. 安装并允许"未知来源"权限（Android 会提示）

*支持 Android 8.0 (API 26) 及以上*

---

## 🚀 三分钟上手

### 1. 添加你的服务器

打开 App → **服务器源URL** → 输入服务器地址

https://noticat.example.com

http://192.168.1.5:8080

*支持 HTTP/HTTPS，支持局域网 IP，随时随地添加*

### 2. 登录/注册

每个服务器独立账号：

- 已有账号：直接登录

- 新服务器：快速注册（数据存储在该服务器）

### 3. 开始订阅

订阅客户端 → **添加订阅** → 选择信息源 → 设置过滤规则 → 坐等邮件

---

## ✨ 核心功能

### 🔗 多服务器管理

- **并列展示**：主页切换不同服务器节点
- **独立会话**：每个服务器保持独立登录态，互不干扰
- **实时状态**：在线/离线一目了然，点击首页图标刷新状态

示例场景：

- 学校服务器（抓取教务处通知）
- 个人服务器（抓取B站UP主动态 + GitHub Releases）
- 公司服务器（内部系统监控）

全部在一个 App 里搞定。

### 📋 订阅管理

- **可视化订阅列表**：开关状态、下次检查时间、过滤规则摘要
- **快速筛选**：未读优先、按源分类、正则关键词高亮
- **即时测试**：输入正则实时预览匹配结果，避免过滤错杀

### 🎨  Material You 设计

- 自动跟随系统取色
- 深色模式
- 简洁设计

---

## 📖 使用指南

### 如何添加多个服务器？

主页 → 服务器源URL → **上滑添加节点**

- 左滑可删除本地缓存（不会删除服务器数据）
- 长按备注别名（如"家里NAS"、"学校VPS"）

### 账号切换？

添加服务器 → 填入服务器地址 → 登录账号

因为允许填入**相同的URL**，所以切换账号如切换服务器一样简单

- 数据隔离，不同账号不同订阅。

### 过滤规则怎么写？

标准的 **正则表达式**（和 Python 类似）：

- 包含"考试"或"选课"：`考试|选课`
- 排除"已过期的"：`^(?!.*已过期).*通知`
- 匹配特定编号：`\[2024-\d{4}\].*`

过滤规则不能为空喵~

---

## 🛠️ 常见问题

**Q: 连不上服务器？**

- 检查 URL 是否带 `http://` 或 `https://`
- 局域网服务器确保手机和电脑在同一 WiFi
- 防火墙是否放行端口（默认 8080）

**Q: 收不到推送通知？**

- NotiCat 的核心是**邮件推送**，App 只是管理端
- 未来有支持 App 推送的可能

**Q: 数据安全吗？**

- 所有账号密码仅保存在**对应服务器**上
- App 本地只缓存 Token（可一键清除）
- 不同服务器之间数据完全隔离

---

## 🔗 相关链接

- [**NotiCat Server**](https://github.com/YOURNAME/noticat-server) - 需要自己部署后端？看这里
- [**反馈问题**](https://github.com/YOURNAME/noticat-android/issues) - Bug 报告或功能建议

---

<div align="center">

**Made with 😼 Kotlin & Jetpack Compose**

</div>