# 🪄 Minecraft Mod Downloader

**Automated mod installer for CurseForge and Modrinth**  
This Java-based tool scrapes mod pages, downloads the latest mod files, and places them directly into organized folders (`mods`, `shaders`, `texture-packs`, etc.). Ideal for quickly setting up a Minecraft instance with multiple mods.

---

## 🚀 Features

- ✅ Automatically downloads mods from [CurseForge](https://www.curseforge.com) and [Modrinth](https://modrinth.com)  
- ✅ Supports **mods**, **shaders**, **texture packs**, **plugins**, and **datapacks**  
- ✅ Categorizes downloads into the correct directories  
- ✅ Headless Chrome automation using **Selenium**  
- ✅ Waits between downloads to reduce bot detection  
- ✅ Logs any failed downloads to `error.log`  

---

## 🛠️ Tech Stack

- **Java**
- **Selenium WebDriver**
- **ChromeDriver** (via WebDriverManager)
- **Headless Chrome**
- **Maven to handle dependencies**
