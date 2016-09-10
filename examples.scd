
(
a = CuePlayer(name: "test");
a.addCue({"cue: 1".postln});
a.addCue({"cue: 2".postln});
a.addCue({"cue: 3".postln});
a.addCue({"cue: 4".postln});
a.addCue({"cue: 5".postln});
a.addCue({"cue: 6".postln});
a.addCue({"cue: 7".postln});
a.addCue({"inserted in place of 3".postln}, 3);
a.gui;
)

a.next;
a.setCurrent(8);
a.cues;
a.guiInstance.window.onClose.value

  /* setCmdPeriodActions { */
  /*   // Run some things on Cmd+Period */
  /*   CmdPeriod.add({SystemClock.sched(0.1, { */
  /*     // create groups */
  /*     groupA = Group.head(Server.default); */
  /*     groupB = Group.after(groupA); */
  /*     groupZ = Group.tail(Server.default); */

  /*     // oscTrigBut.value; */
  /*     // midiFunc.value; */
  /*     // oscInputLevels.value; // the osc responder */

  /*     sig_meter = Synth(\sig_meter, [\in1, input1, \in2, input2], target: groupA ); */
  /*     // the synth which sends amplitude data */

  /*     timer.stop; */

  /*     // Pdef.all.clear; // clear all pdefs */
  /*   })}); */
  /* } */
