
+ String {

  asTimeline { arg clock, options; var array;
    array = this.standardizePath.load;
    ^Timeline.newFromArray(array, clock, options);
  }

}

+ Array {

  asTimeline { arg clock, options;
    ^Timeline.newFromArray(this, clock, options);
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
