
+ String {

  asFunkySchedulerCP { arg clock, mode; var array;
    array = this.standardizePath.load;
    ^FunkySchedulerCP.newFromArray(array, clock, mode);
  }

}

+ Array {

  asFunkySchedulerCP { arg clock, mode;
    ^FunkySchedulerCP.newFromArray(this, clock, mode);
  }

}

+ Object {

  asFunkySchedulerCP {
    "This object can not be used as a CuePlayer timeline".warn;
  }

}

+ Nil {

  asFunkySchedulerCP {
    ^nil
  }

}
