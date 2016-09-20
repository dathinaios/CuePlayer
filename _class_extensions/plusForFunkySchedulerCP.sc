
+ String {

  asFunkySchedulerCP { arg clock; var array;
    array = this.standardizePath.load;
    ^FunkySchedulerCP.newFromArray(array, clock);
  }

}

+ Array {

  asFunkySchedulerCP { arg clock;
    ^FunkySchedulerCP.newFromArray(this, clock);
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
