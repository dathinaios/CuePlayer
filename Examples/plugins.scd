/*
PluginCP examples.

Built-in plugins ship in the quark's Plugins/ folder and are loaded by symbol key.
Custom plugins are loaded from a file path; the id is derived from the filename.

Run the block, then try:
- a.next;
- a.trigger(2);
- a.tempo_(100);

Expected:
- the GUI opens with a "Basic Plugin" strip
- the strip text updates to show the latest interaction
*/

(
a = CuePlayer.new;

a.put(1, { "cue 1 fired".postln });
a.put(2, { "cue 2 fired".postln });

a.gui(
	options: (
		plugins: [\basic],
	)
);
)

// Inspect the plugin instance
a.guiInstance.pluginAt(\basic);

// Inspect stored state (should return StaticText, not nil)
a.guiInstance.pluginAt(\basic).at(\label);

// Trigger interactions - the plugin label should update each time
a.next;
a.trigger(2);
a.tempo_(100);

/* ------------------------------------------------------------------ */
// DurationLine strip plugin (built-in)

(
b = CuePlayer.new;

b.gui(
	options: (
		largeDisplay: true,
		largeDisplayOptions: (bounds: Rect(1400, 210, 285, 285)),
		plugins: [\durationLine]
	)
);

~durLine = DurationLine(b);

b.put(1, {
	"cue-1".postln;
	~durLine.create(duration: 1, color: Color.red, width: 30);
});

b.put(2, {
	"cue-2".postln;
	~durLine.create(duration: 3, color: Color.blue, width: 15);
});

b.put(3, {
	"cue-3".postln;
	~durLine.create(duration: 8, color: Color.green, width: 6);
});
)

b.trigger(1);
b.trigger(2);

/* ------------------------------------------------------------------ */
// Custom plugin from a user-supplied file
// The id is derived from the filename (e.g. my_plugin.scd → \my_plugin)

(
c = CuePlayer.new;
c.gui(options: (plugins: ["./path/to/my_plugin.scd"]));
)
