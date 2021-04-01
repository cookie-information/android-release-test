# Changelog

## Unreleased

### Added
- Added `required` and `type` fields in `ConsentItem`.
- Added support for suggested texts for use in the user interface.
- Added possibility to observe the "save consents" events.
- Added `BasePrivacyCenterFragment` and `BasePrivacyPreferencesDialogFragment` as base of the user interface elements to integrate in target application.

### Changed
- Exposed coroutine API. Refactored `MobileConsentSdk` to use it with coroutines and `CallbackMobileConsentSdk` with callbacks.
- Changes in `ConsentItem` class regarding text translations.
- Fragment navigation in "sample app".

## v0.1.1 - 2020-11-26

### Added
n/a
### Changed

Url for release version changed to production server.

## v0.1.0 - 2020-10-21

### Added
n/a
### Changed
- `Translation` class renamed to `ConsentTranslation`.
- `getConsentSolution` method renamed to `fetchConsentSolution`.
- `getConsentChoices` method renamed to `getSavedConsents`.
- `getConsentChoice` method renamed to `getSavedConsent`.
