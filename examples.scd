
(
a = CuePlayer(name: "test");
a.cues.addCue({"cue: 1".postln});
a.cues.addCue({"cue: 2".postln});
a.cues.addCue({"cue: 3".postln});
a.cues.addCue({"cue: 4".postln});
a.cues.addCue({"cue: 5".postln});
a.cues.addCue({"cue: 6".postln});
a.cues.addCue({"cue: 7".postln});
a.gui;
)

a.next;
a.setCurrent(8);
a.cues;
a.guiInstance.window.onClose.value
