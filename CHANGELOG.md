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
- Fixed URL warnings

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