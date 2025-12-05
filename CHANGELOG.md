# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- GitLab repository support
- Bitbucket repository support
- Alternative library suggestions

## [1.0.0] - 2024-12-06

### Added
- 🎉 Initial release of Go Archived Library Detector
- ✅ Automatic detection of archived GitHub repositories
- ⚠️ Visual warnings in editor for archived imports
- 🔍 Code inspection for archived libraries
- 💾 Smart local caching system (24 hours default)
- 📊 Batch checking for all project imports
- 🔑 Configurable GitHub token support (60 → 5000 requests/hour)
- 🔄 Background update task for periodic checks
- 🌍 Internationalization support (English and Russian)
- 🛠️ Quick fix to remove archived imports
- 🧹 Clear cache action
- ⚙️ Flexible configuration options (cache duration, batch size, update interval)## [1.0.1] - 2024-12-04

## [1.0.1] - 2024-12-04

### Fixed
- Removed usage of deprecated URL(String) constructor in ArchiveCheckService.
- Migrated HTTP initialization to the recommended URI.create(...).toURL() API.
- Improved compatibility with Java 21+ and IntelliJ Platform 2025 networking requirements.
- Minor internal refactor of GitHub repository status checks.
- Improved stability of API requests and updated error-handling behaviour.

## [1.0.2] - 2024-12-04

### Fix deprecated API usage
- Replaced deprecated JPasswordField.getText() with secure getPassword() in PluginSettingsConfigurable.isModified().
- Ensured safe handling of password char[] with explicit memory cleanup.
- Improved compatibility with IntelliJ Platform 2025+ Plugin Verifier.
- Removed deprecated URL(String) constructor usage in ArchiveCheckService (replaced with URI.toURL()).
- General code cleanup and internal stability improvements.

## [1.0.3] - 2024-12-05

### Add CICD actions for release
- Added GitHub Actions for release automation

### Features
- Real-time import analysis as you code
- Performance optimized with intelligent caching
- Multi-language interface
- Project-wide scanning capability
- Minimal memory footprint (~5MB)

### Technical Details
- Built with IntelliJ Platform SDK 2.1.0
- Kotlin 1.9.22
- Supports GoLand 2024.2 and later
- Requires JDK 17 or higher

### Documentation
- Comprehensive README with installation and usage guide
- CONTRIBUTING guide for developers
- Development documentation
- Issue templates for bugs and features
- Pull request template

---

## [0.9.0] - 2024-11-15 (Beta)

### Added
- Beta release for testing
- Basic GitHub API integration
- Simple caching mechanism
- English-only interface

### Fixed
- Initial bug fixes from alpha testing
- Performance improvements
- Memory leak in cache service

---

## [0.1.0] - 2024-11-01 (Alpha)

### Added
- Alpha release for internal testing
- Proof of concept for archived library detection
- Basic UI integration

---

### [1.1.0] - Planned
- Support for GitLab repositories
- Support for Bitbucket repositories
- Alternative library suggestions
- Library health metrics
- Custom repository lists

### [1.2.0] - Planned
- Integration with go.mod
- Dependency tree analysis
- Auto-update suggestions
- Export reports feature

## Future Releases

### [1.1.0] - Planned for Q1 2025

#### Planned Features
- GitLab repository support
- Bitbucket repository support
- Custom repository lists (whitelist/blacklist)
- Enhanced notification system
- Improved error messages
- Better rate limit handling
- Export scan results to file

#### Technical Improvements
- Optimized API call batching
- Reduced memory usage
- Faster startup time
- Better error recovery

### [1.2.0] - Planned for Q2 2025

#### Planned Features
- Alternative library suggestions when archived library found
- Library health metrics (last commit, open issues, etc.)
- Integration with go.mod for automatic detection
- Dependency tree analysis
- Custom warning messages
- Team sharing of library preferences

#### UI Improvements
- Settings page redesign
- Interactive notifications
- Dashboard for statistics

### [2.0.0] - Planned for Q3 2025

#### Major Features
- AI-powered library recommendations
- Automated migration suggestions
- Team collaboration features
- Advanced analytics dashboard
- Plugin API for extensions
- Integration with popular CI/CD tools

#### Breaking Changes
- Minimum GoLand version: 2025.1
- New configuration format
- API changes for extensions

---

## Version History

| Version | Date | Status | Notes |
|---------|------|--------|-------|
| 1.0.0 | 2024-12-06 | Stable | Initial public release |
| 0.9.0 | 2024-11-15 | Beta | Beta testing phase |
| 0.1.0 | 2024-11-01 | Alpha | Internal testing |

---

## How to Update

### Automatic (Recommended)
1. GoLand will notify you when update is available
2. Click "Update" in notification
3. Restart GoLand

### Manual
1. Download latest version from [Releases](https://github.com/zhasulan/go-archived-library-detector-idea-plugin/releases)
2. Go to Settings → Plugins → ⚙️ → Install Plugin from Disk
3. Select downloaded ZIP file
4. Restart GoLand

---

## Deprecation Notices

### None Currently

We will announce deprecations at least one major version in advance.

---

## Support

- 🐛 [Report bugs](https://github.com/zhasulan/go-archived-library-detector-idea-plugin/issues/new?template=bug_report.md)
- 💡 [Request features](https://github.com/zhasulan/go-archived-library-detector-idea-plugin/issues/new?template=feature_request.md)
- 💬 [Discussions](https://github.com/zhasulan/go-archived-library-detector-idea-plugin/discussions)
- 📧 Email: your.email@example.com

---

**Note**: Dates are in YYYY-MM-DD format following ISO 8601.