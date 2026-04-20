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
