# Changelog

All notable changes to this project will be documented in this file.

Historical entries before this file was added were backfilled from the existing GitHub release notes. The current top entry covers all changes on `HEAD` since tag `1.1.0`.

## Unreleased

- Added a GUI plugin system via `CuePlayer.gui(options: (plugins: ...))`. See `PluginCP` help and `Examples/Plugins/`.
- `DurationLine` now also renders inside the main GUI when the duration-line plugin is loaded (previously only drew on the large window).
- Breaking: extracted the large performer window from `CueTriggerCP` into a new `LargeDisplayCP` class. Code that reached it through `gui.cueTrigger` (for example `gui.cueTrigger.lrgCueWin`) must now use `gui.largeDisplay`.
- Deprecated `largeDisplayBounds` in favour of `largeDisplayOptions: (bounds: ...)`. The old key still works and emits a warning.
- Added styling options for the large window under `largeDisplayOptions`: `bounds`, `background`, `textColor`, `infoBackground`, `infoTextColor`, `cueNumberSize`, `infoTextSize`.

## 1.1.0 - 2026-03-08

- Various bug fixes.

## 1.0.0 - 2023-02-09

- Implemented offset argument for `Timeline`.
- Implemented setter for server volume.

## 0.2.4 - 2020-04-11

- Added methods for global deactivation of `DurationLine` instances.
- Renamed `activate`/`deactivate` methods to `on`/`off` and added `toggle`.
- Allowed `CuePlayer` method calls from inside a cue.
- Fixed large window value being displayed as a float.

## 0.2.3 - 2018-10-12

- Added `DurationLine`, creating a vertical line on the large window which scrolls from left to right over a specified duration.
- Added `addMIDIFunc` to support custom MIDI controls inside the CuePlayer MIDI management system.
- Changed timeline argument defaults to `( mode: \time, quant: 0 )`.
- Fixed timer start on foot-switch trigger.
- Fixed a crash when the large window was closed after being initiated.

## 0.2.2 - 2018-03-06

- Added `midiTriggerControl` for evaluating sequential cues from incoming MIDI control messages.
- Renamed `midiTrigger` to `midiTriggerNoteOn`; the old name is deprecated.

## 0.2.1 - 2017-09-30

- Fixed `.add`.
- Allowed access to the large display window through the `lrgCueWin` variable. See issue `#8`.

## 0.2.0 - 2016-12-14

- Added support for displaying information. See `CueInfo`, the `CuePlayer` help file, and the updated tutorial for details.
- Various bug fixes.

## 0.1.1 - 2016-11-04

- Fixed a GUI crash when the server is not booted.

## 0.1.0 - 2016-11-04

- Initial tagged release.
