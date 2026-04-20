// Minimal interactive plugin example.
// Returns an options Event consumed by CuePlayerGUI.
(
	name: "Basic Plugin",
	height: 30,
	build: { arg plug; var label;
		label = StaticText(plug.contentView, Rect(8, 5, 260, 20));
		label.string_("plugin built; waiting for interaction");
		label.stringColor_(Color.white);
		plug.store(\label, label);
		"build ok".postln;
	},
	onCue: { arg plug, cp, msg; var label, text;
		label = plug.at(\label);
		text = switch(msg)
		{\current} { "current cue set to " ++ cp.current }
		{\next}    { "triggered next; now at cue " ++ cp.current }
		{\tempo}   { "tempo changed to " ++ (cp.clock.tempo * 60).round(0.1) ++ " bpm" }
		{ "received update: " ++ msg };
		label.string_(text);
		("onCue received: " ++ msg).postln;
	},
	onClear: { arg plug;
		"onClear ok".postln;
	}
)
