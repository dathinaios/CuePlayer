
Cues { var <>cueList, <>current, >hook;

  *new {
    ^super.new.init;
  }

  init {
    current = 0;
    cueList = Array.new;
  }

  add { arg function;
    cueList.add(function);
  }

  put {arg cueNumber = (cueList.size + 1), function;
    if(cueList.size < cueNumber) {
      cueList = cueList.extend(cueNumber, nil);
    };
    cueList[cueNumber - 1] = function;
  }

  next {
    hook.value(this);
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
    /*("Next cue will be " ++ (cue + 1)).postln;*/
    current = cue.abs;
    this.changed(\current);
		^current;
  }

}
