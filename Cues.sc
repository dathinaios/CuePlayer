
Cues { var <>cueList, <>current;

  *new {
    ^super.new.init;
  }

  init {
    current = 0;
    cueList = Array.new;
  }

  add { arg function, cueNumber = (cueList.size + 1);
    if(cueList.size < cueNumber) {
      cueList = cueList.extend(cueNumber, nil);
    }; 
    cueList[cueNumber - 1] = function;
    ^cueList.size;
  }

  next {
    cueList[current].value;
    current = current + 1;
    this.changed(\current);
    ^current;
  }

  trigger { arg cue = 1;
    this.setCurrent(cue);
    ^this.next;
  }

  setCurrent {arg cue;
    ("Next cue will be " ++ cue).postln;
    current = cue.abs - 1;
    this.changed(\current);
  }

}
