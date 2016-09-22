
+ String {

  asTimeline { arg clock, mode; var array;
    array = this.standardizePath.load;
    ^Timeline.newFromArray(array, clock, mode);
  }

}

+ Array {

  asTimeline { arg clock, mode;
    ^Timeline.newFromArray(this, clock, mode);
  }

}

+ Object {

  asTimeline {
    "This object can not be used as a CuePlayer timeline".warn;
  }

}

+ Nil {

  asTimeline {
    ^nil
  }

}
