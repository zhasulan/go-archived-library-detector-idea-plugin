# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2024-12-04

### Added
- Initial release
- Automatic detection of archived GitHub repositories
- Visual warnings in editor for archived imports
- Code inspection for archived libraries
- Smart local caching (24 hours default)
- Batch checking for all project imports
- Configurable GitHub token support
- Background update task
- i18n support (English and Russian)
- Quick fix to remove archived imports
- Clear cache action
- Configurable cache duration and batch size

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

### Features
- 🔍 Real-time import analysis
- ⚡ Performance optimized with smart caching
- 🌍 Multi-language support
- 📊 Project-wide scanning
- ⚙️ Flexible configuration options

### Technical
- Supports GoLand 2024.2+
- Built with IntelliJ Platform SDK 2.1.0
- Kotlin 1.9.22
- JDK 17

## [Future Plans]

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