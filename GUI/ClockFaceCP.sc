/* 
This class is modified from the ClockFace class created by Josh Parmenter.
The original can be found at: https://github.com/supercollider-quarks/ClockFace 
*/

ClockFaceCP {
  var <window, <starttime, <tempo, <>inc, <>bounds, <cursecs, <>isPlaying = false, clock, timeString;

  *new{ 
    arg window, starttime = 0, tempo = 1, inc = 0.1, bounds = Rect(0, 0, 209, 20);
    ^super.newCopyArgs(window, starttime, tempo, inc, bounds).init;
  }

  init {
    cursecs = starttime;
    this.digitalGUI;
  }

  play {
    var start;
    clock = TempoClock.new(tempo);
    start = clock.elapsedBeats;
    clock.sched(inc, {
      this.cursecs_(clock.elapsedBeats - start + starttime, false);
      inc;
    });
    isPlaying = true;
  }

  cursecs_ {arg curtime, updateStart = true;
    var curdisp;
    cursecs = curtime;
    curdisp = curtime.asTimeString;
    curdisp = curdisp[0 .. (curdisp.size-3)];
    updateStart.if({starttime = cursecs});
    {timeString.string_(curdisp)}.defer;
  }

  stop {
    starttime = cursecs;
    clock.clear;
    clock.stop;
    isPlaying = false;
  }

  digitalGUI { var initialValue;
    initialValue = "00:00:00:0";
    timeString = GUI.staticText.new(window, bounds)
    .string_(initialValue)
    .font_(Font("Arial", 20))
    .align_(\center)
    .stringColor_(Color.white);
  }

}

