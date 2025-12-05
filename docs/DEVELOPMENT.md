# 🛠️ Development Guide

Complete guide for developers who want to contribute or understand the codebase.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Setting Up](#setting-up)
- [Project Structure](#project-structure)
- [Building](#building)
- [Testing](#testing)
- [Debugging](#debugging)
- [Architecture](#architecture)
- [Adding Features](#adding-features)

## Prerequisites

### Required

- **JDK 17 or higher**
  ```bash
  # Check version
  java -version
  
  # Should show: java version "17.0.x" or higher
  ```

- **GoLand 2024.2 or higher** (for testing)
  - Download from: https://www.jetbrains.com/go/

- **Git**
  ```bash
  git --version
  ```

### Recommended

- **IntelliJ IDEA Ultimate** (for plugin development)
  - Better support for plugin development
  - Built-in tools and debugger

## Setting Up

### 1. Clone Repository

```bash
# Clone your fork
git clone https://github.com/YOUR-USERNAME/go-archived-detector.git
cd go-archived-detector

# Add upstream remote
git remote add upstream https://github.com/ORIGINAL-OWNER/go-archived-detector.git
```

### 2. Open in IDE

**Option A: IntelliJ IDEA (Recommended)**
1. Open IntelliJ IDEA
2. File → Open → Select project directory
3. Wait for Gradle sync to complete

**Option B: Command Line**
```bash
# Build to verify setup
./gradlew build
```

### 3. Run Plugin in Development Mode

```bash
# This launches GoLand with your plugin installed
./gradlew runIde
```

## Project Structure

```
go-archived-library-detector/
├── build.gradle.kts              # Build configuration
├── settings.gradle.kts           # Gradle settings
├── gradle.properties             # Gradle properties
│
├── src/
│   ├── main/
│   │   ├── kotlin/com/github/goarchived/
│   │   │   ├── action/           # User-triggered actions
│   │   │   │   ├── CheckAllImportsAction.kt
│   │   │   │   └── ClearCacheAction.kt
│   │   │   │
│   │   │   ├── annotator/        # Code highlighting
│   │   │   │   └── ArchivedLibraryAnnotator.kt
│   │   │   │
│   │   │   ├── background/       # Background tasks
│   │   │   │   └── BackgroundUpdateTask.kt
│   │   │   │
│   │   │   ├── inspection/       # Code inspections
│   │   │   │   └── ArchivedLibraryInspection.kt
│   │   │   │
│   │   │   ├── service/          # Core business logic
│   │   │   │   ├── ArchiveCheckService.kt
│   │   │   │   └── LocalCacheService.kt
│   │   │   │
│   │   │   ├── settings/         # Configuration
│   │   │   │   ├── PluginSettings.kt
│   │   │   │   └── PluginSettingsConfigurable.kt
│   │   │   │
│   │   │   └── util/             # Utilities
│   │   │       └── GoArchivedBundle.kt
│   │   │
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml    # Plugin manifest
│   │       └── messages/         # i18n files
│   │           ├── GoArchivedBundle.properties
│   │           └── GoArchivedBundle_ru.properties
│   │
│   └── test/                     # Tests
│       └── kotlin/
│           └── com/github/goarchived/service/
│               ├── ArchiveCheckServiceTest.kt
│               └── LocalCacheServiceTest.kt
│
├── docs/                         # Documentation
├── .github/                      # GitHub configuration
└── gradle/                       # Gradle wrapper
```

## Building

### Basic Build

```bash
# Clean and build
./gradlew clean build

# Output: build/distributions/go-archived-library-detector-*.zip
```

### Build Without Tests

```bash
./gradlew buildPlugin -x test
```

### Verify Plugin

```bash
# Check plugin compatibility with IDE versions
./gradlew runPluginVerifier
```

## Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test

```bash
./gradlew test --tests "ArchiveCheckServiceTest"
./gradlew test --tests "*Cache*"
```

### Run with Coverage

```bash
./gradlew test jacocoTestReport

# View report: build/reports/jacoco/test/html/index.html
```

### Writing Tests

Example test structure:

```kotlin
class ArchiveCheckServiceTest : BasePlatformTestCase() {
    
    private lateinit var service: ArchiveCheckService
    
    override fun setUp() {
        super.setUp()
        service = project.getService(ArchiveCheckService::class.java)
    }
    
    fun testCheckValidRepository() {
        val result = service.checkRepository("github.com/golang/go")
        assertNotNull(result)
        assertFalse(result!!.isArchived)
    }
}
```

## Debugging

### Debug Plugin in GoLand

```bash
# Start in debug mode
./gradlew runIde --debug-jvm
```

Then in IntelliJ IDEA:
1. Run → Edit Configurations
2. Add → Remote JVM Debug
3. Port: 5005
4. Click Debug

### View Logs

**In Test GoLand Instance:**
1. Help → Show Log in Finder/Explorer
2. Search for "GoArchived" entries

**Enable Debug Logging:**
1. Help → Diagnostic Tools → Debug Log Settings
2. Add: `com.github.goarchived`
3. Click OK
4. Reproduce issue
5. Check logs

### Common Debug Points

```kotlin
// Add breakpoints here:
class ArchiveCheckService {
    fun checkRepository(importPath: String): RepositoryStatus? {
        val cached = localCache.getCached(importPath)  // 👈 Breakpoint
        if (cached != null) return cached
        
        val status = checkGitHubRepository(owner, repo)  // 👈 Breakpoint
        return status
    }
}
```

## Architecture

### Component Diagram

```
┌─────────────────────────────────────┐
│         GoLand Editor               │
│  (User opens .go file with imports) │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│    ArchivedLibraryAnnotator         │
│  (Highlights archived imports)      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      ArchiveCheckService            │
│  (Checks if library is archived)    │
└──────────────┬──────────────────────┘
               │
        ┌──────┴──────┐
        ▼             ▼
┌─────────────┐  ┌──────────────┐
│  LocalCache │  │  GitHub API  │
│   Service   │  │              │
└─────────────┘  └──────────────┘
```

### Data Flow

1. **User opens file** → PSI tree parsed
2. **Annotator triggered** → Scans imports
3. **Check cache** → LocalCacheService
4. **If not cached** → API call to GitHub
5. **Store result** → Update cache
6. **Show warning** → If archived

## Adding Features

### Example: Add GitLab Support

**Step 1: Update ArchiveCheckService**

```kotlin
private fun checkGitLabRepository(owner: String, repo: String): RepositoryStatus? {
    val url = URL("https://gitlab.com/api/v4/projects/${owner}%2F${repo}")
    val connection = url.openConnection() as HttpURLConnection
    
    connection.requestMethod = "GET"
    connection.setRequestProperty("PRIVATE-TOKEN", gitlabToken)
    
    // ... implementation
}
```

**Step 2: Update parseImportPath**

```kotlin
private fun parseImportPath(importPath: String): RepoInfo? {
    return when {
        importPath.startsWith("github.com/") -> {
            val parts = importPath.split("/")
            RepoInfo("github", parts[1], parts[2])
        }
        importPath.startsWith("gitlab.com/") -> {
            val parts = importPath.split("/")
            RepoInfo("gitlab", parts[1], parts[2])
        }
        else -> null
    }
}
```

**Step 3: Add Tests**

```kotlin
fun testCheckGitLabRepository() {
    val result = service.checkRepository("gitlab.com/user/repo")
    assertNotNull(result)
}
```

**Step 4: Update Documentation**

- README.md: Mention GitLab support
- CHANGELOG.md: Add to [Unreleased]

### Example: Add New Translation

**Step 1: Create Bundle File**

```bash
touch src/main/resources/messages/GoArchivedBundle_de.properties
```

**Step 2: Translate Keys**

```properties
# GoArchivedBundle_de.properties
plugin.name=Go Archivierte Bibliothek Detektor
inspection.archived.library.name=Archivierte Go Bibliothek
# ... all other keys
```

**Step 3: Test**

```bash
./gradlew runIde -Duser.language=de -Duser.country=DE
```

## Performance Tips

### Do's ✅

- **Use caching aggressively**
  ```kotlin
  val cached = cache.get(key)
  if (cached != null) return cached
  ```

- **Background threads for API calls**
  ```kotlin
  ApplicationManager.getApplication().executeOnPooledThread {
      // API call here
  }
  ```

- **Batch operations**
  ```kotlin
  imports.chunked(50).forEach { batch ->
      checkBatch(batch)
  }
  ```

### Don'ts ❌

- **Never block UI thread**
  ```kotlin
  // ❌ Bad
  fun annotate(element: PsiElement, holder: AnnotationHolder) {
      val result = checkRepository(element)  // Blocks UI!
  }
  
  // ✅ Good
  fun annotate(element: PsiElement, holder: AnnotationHolder) {
      executeOnPooledThread {
          val result = checkRepository(element)
          invokeLater { updateUI(result) }
      }
  }
  ```

- **Don't make unnecessary API calls**
  ```kotlin
  // ❌ Bad - Checks every time
  fun check(path: String) = api.check(path)
  
  // ✅ Good - Uses cache
  fun check(path: String) = cache.get(path) ?: api.check(path)
  ```

## Common Issues

### Issue: Gradle Sync Fails

```bash
# Solution 1: Refresh dependencies
./gradlew clean --refresh-dependencies

# Solution 2: Clear cache
rm -rf ~/.gradle/caches
./gradlew build
```

### Issue: Plugin Doesn't Load

Check `idea.log` for errors:
```
Plugin 'com.github.goarchived' failed to load
```

**Solutions:**
- Verify `plugin.xml` syntax
- Check all required dependencies
- Ensure correct package names

### Issue: Tests Fail on CI

**Common causes:**
- Timezone differences
- File path separators (Windows vs Unix)
- Network timeouts

**Solutions:**
```kotlin
// Use system-independent paths
val file = File.separator + "path" + File.separator + "to" + File.separator + "file"

// Mock network calls in tests
whenever(api.check(any())).thenReturn(mockResult)
```

## Resources

- [IntelliJ Platform SDK Docs](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Kotlin Reference](https://kotlinlang.org/docs/reference/)
- [Go Plugin Sources](https://github.com/go-lang-plugin-org/go-lang-idea-plugin)
- [JetBrains Platform Slack](https://plugins.jetbrains.com/slack)

## Getting Help

- 💬 [GitHub Discussions](https://github.com/zhasulan/go-archived-library-detector-idea-plugin/discussions)
- 🐛 [Report Issue](https://github.com/zhasulan/go-archived-library-detector-idea-plugin/issues)
- 📧 Email: your.email@example.com

---

**Happy coding! 🚀**