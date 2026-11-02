# Changelog

## [Unreleased]

## [1.0.1] - 2026-11-02

### Fixed

- Fix missing context menu actions in RustRover by adding them to the Other submenu

## [1.0.0]

### Added

- Copy selected files and folders with content as XML-formatted LLM prompt
  - Copy project structure without content
  - Copy all open editor tabs to prompt
  - Copy problems (errors/warnings) from current line or whole file, with or without file content
  - `.gitignore` and nested ignore files support with confirmation dialog for ignored items
  - `.c2pignore` for plugin-specific overrides
  - Binary and oversized file filtering, token count estimation

[Unreleased]: https://github.com/0zd0/context2prompt/compare/v1.0.1...HEAD
[1.0.1]: https://github.com/0zd0/context2prompt/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/0zd0/context2prompt/commits/v1.0.0
