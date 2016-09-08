
(
a = CuePlayer(name: "test");
a.cues.addCue({"cue: 1".postln});
a.cues.addCue({"cue: 2".postln});
a.cues.addCue({"cue: 3".postln});
a.cues.addCue({"cue: 4".postln});
a.cues.addCue({"cue: 5".postln});
a.cues.addCue({"cue: 6".postln});
a.cues.addCue({"cue: 7".postln});
)

a.next;
a.setCurrent(1)
a.gui;

a.cueList

b = Cues.new;
b.next;
(
b.addCue({("yahoo" + 43.rand).postln});
b.addCue({("yahoo" + 43.rand).postln});
b.addCue({("yahoo" + 43.rand).postln});
b.addCue({("yahoo" + 43.rand).postln});
b.addCue({("yahoo" + 43.rand).postln});
b.addCue({("yahoo" + 43.rand).postln});
b.addCue({("yahoo" + 43.rand).postln});
)

b.cueList
