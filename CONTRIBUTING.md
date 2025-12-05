# Contributing to Go Archived Library Detector

First off, thank you for considering contributing! 🎉

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)

## 📜 Code of Conduct

This project adheres to a Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to your.email@example.com.

### Our Standards

- Be respectful and inclusive
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards other community members

## 🤝 How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues. When creating a bug report, include:

- **Clear title**: Descriptive and specific
- **Description**: Detailed explanation of the issue
- **Steps to Reproduce**: Step-by-step instructions
- **Expected Behavior**: What you expected to happen
- **Actual Behavior**: What actually happened
- **Environment**: 
  - OS (macOS, Windows, Linux)
  - GoLand version
  - Plugin version
  - Go version
- **Logs**: Relevant error logs from `idea.log`
- **Screenshots**: If applicable

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- **Use a clear title**
- **Provide detailed description** of the proposed feature
- **Explain why this would be useful** to most users
- **Include mockups or examples** if applicable
- **List alternative solutions** you've considered

### Your First Code Contribution

Unsure where to start? Look for issues labeled:
- `good first issue` - Simple issues for beginners
- `help wanted` - Issues that need attention
- `documentation` - Documentation improvements

## 🛠️ Development Setup

### Prerequisites

- JDK 17 or higher
- GoLand 2024.2 or higher
- Git

### Setup Steps

```bash
# 1. Fork the repository on GitHub

# 2. Clone your fork
git clone https://github.com/YOUR-USERNAME/go-archived-detector.git
cd go-archived-detector

# 3. Add upstream remote
git remote add upstream https://github.com/ORIGINAL-OWNER/go-archived-detector.git

# 4. Create a branch
git checkout -b feature/your-feature-name

# 5. Build the project
./gradlew build

# 6. Run in test IDE
./gradlew runIde
```

## 🔄 Pull Request Process

### Before Submitting

1. **Update documentation** if needed
2. **Add tests** for new features
3. **Run tests**: `./gradlew test`
4. **Check code style**: Follow Kotlin conventions
5. **Update CHANGELOG.md** with your changes

### Submitting

1. **Push to your fork**
```bash
git push origin feature/your-feature-name
```

2. **Open Pull Request** on GitHub

3. **Fill in the PR template** with:
   - Description of changes
   - Related issue number
   - Type of change
   - Testing done
   - Checklist completion

4. **Wait for review**
   - Address reviewer feedback
   - Keep PR up to date with main branch

### PR Title Format

Use conventional commits:
- `feat: add GitLab support`
- `fix: resolve cache invalidation issue`
- `docs: update README with new features`
- `test: add tests for ArchiveCheckService`
- `refactor: improve cache performance`
- `chore: update dependencies`

## 💻 Coding Standards

### Kotlin Style

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ Good
class ArchiveCheckService(private val project: Project) {
    fun checkRepository(path: String): RepositoryStatus? {
        return cache.get(path) ?: fetchFromApi(path)
    }
}

// ❌ Bad
class ArchiveCheckService(private val project:Project){
    fun checkRepository(path:String):RepositoryStatus?{
        return cache.get(path)?:fetchFromApi(path)
    }
}
```

### Naming Conventions

- **Classes**: PascalCase (e.g., `ArchiveCheckService`)
- **Functions**: camelCase (e.g., `checkRepository`)
- **Variables**: camelCase (e.g., `repositoryStatus`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_CACHE_SIZE`)

### Comments

```kotlin
// ✅ Good - Explains WHY
// Cache duration set to 24 hours to balance freshness with API limits
val cacheDuration = TimeUnit.HOURS.toMillis(24)

// ❌ Bad - Explains WHAT (obvious)
// Set cache duration to 24 hours
val cacheDuration = TimeUnit.HOURS.toMillis(24)
```

### KDoc Comments

```kotlin
/**
 * Checks if a repository is archived.
 *
 * @param importPath The import path (e.g., "github.com/user/repo")
 * @return Repository status or null if check fails
 */
fun checkRepository(importPath: String): RepositoryStatus?
```

## 🧪 Testing Guidelines

### Writing Tests

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

### Test Coverage

- Aim for at least 70% code coverage
- Test happy paths AND edge cases
- Test error handling

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "ArchiveCheckServiceTest"

# Run with coverage
./gradlew test jacocoTestReport
```

## 🌍 Adding Translations

### Steps to Add a New Language

1. **Create properties file**
```bash
touch src/main/resources/messages/GoArchivedBundle_de.properties
```

2. **Translate all keys** from `GoArchivedBundle.properties`

Example for German:
```properties
plugin.name=Go Archivierte Bibliothek Detektor
inspection.archived.library.name=Archivierte Go Bibliothek
settings.display.name=Archivierte Bibliothek Detektor
```

3. **Test with locale**
```bash
./gradlew runIde -Duser.language=de -Duser.country=DE
```

## 🔌 Adding Support for New Hosting Providers

### Example: Adding GitLab Support

1. **Extend ArchiveCheckService**
```kotlin
private fun checkGitLabRepository(owner: String, repo: String): RepositoryStatus? {
    val url = URL("https://gitlab.com/api/v4/projects/${owner}%2F${repo}")
    // Implementation
}
```

2. **Update parseImportPath**
```kotlin
private fun parseImportPath(importPath: String): RepoInfo? {
    return when {
        importPath.startsWith("github.com/") -> parseGitHub(importPath)
        importPath.startsWith("gitlab.com/") -> parseGitLab(importPath)
        else -> null
    }
}
```

3. **Add tests**
4. **Update documentation**

## 📝 Commit Message Guidelines

Format: `<type>(<scope>): <subject>`

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructuring
- `test`: Tests
- `chore`: Maintenance

Examples:
```
feat(gitlab): add GitLab repository support
fix(cache): resolve cache invalidation on settings change
docs(readme): update installation instructions
test(service): add tests for batch checking
```

## 🎯 Project Structure

Understanding the codebase:

```
src/main/kotlin/com/github/goarchived/
├── action/           # User-triggered actions
├── annotator/        # Code highlighting
├── background/       # Background tasks
├── inspection/       # Code inspections
├── service/          # Core business logic
├── settings/         # Configuration UI
└── util/            # Utilities (i18n, etc.)
```

## 🆘 Getting Help

- **Questions?** Open a [Discussion](https://github.com/zhasulan/go-archived-library-detector-idea-plugin/discussions)
- **Bug?** Open an [Issue](https://github.com/zhasulan/go-archived-library-detector-idea-plugin/issues)
- **Chat?** Join our [Slack/Discord] (if available)

## 🏆 Recognition

Contributors are recognized in:
- CHANGELOG.md for each release
- README.md contributors section
- GitHub contributors graph

Thank you for contributing! 🙏