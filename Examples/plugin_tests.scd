/*
Minimal PluginCP test using a file-based plugin definition.

The plugin spec lives in Plugins/basic.scd and is loaded through
gui(options: (plugins: ...)).

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
		plugins: [\basic, "./Plugins/basic.scd"],
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
// DurationLine strip plugin
// The same DurationLine.create call renders on both the large performer
// window and the strip inside the main GUI.

(
b = CuePlayer.new;

b.gui(
	options: (
		largeDisplay: true,
		largeDisplayBounds: Rect(1400, 210, 285, 285),
		plugins: [\durationLine, "./Plugins/duration_line.scd"]
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
