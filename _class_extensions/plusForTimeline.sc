
+ String {

  asTimeline { arg clock, options;
    ^Timeline.newFromPath(this, clock, options);
  }

}

+ Array {

  asTimeline { arg clock, options;
    ^Timeline.newFromArray(this, clock, options);
  }

}

+ Object {

  asTimeline {
    "------------------------------------------------------------".postln;
    (this.class.asString + "does not respond to asTimeline").warn;
    "Use one of the following objects:".postln;
    "    Array (with time-function pairs)".postln;
    "    String (path to scd file that returns an array as above)".postln;
    "------------------------------------------------------------".postln;
  }

}

+ Nil {

  asTimeline {
    ^nil
  }

}
