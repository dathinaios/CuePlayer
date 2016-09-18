
Cues { var <>cueList, <>current;
  
  *new {
    ^super.new.init;
  }

  init {
    current = 0;
    cueList = Array.new;
  }

  next {
    cueList[current].value;
    current = current + 1;
    this.changed(\current);
    ^current;
  }

  trigger { arg cue = 1;
      this.setCurrent(cue - 1);
      ^this.next;
  }

  addCue { arg function, cueNumber;
    if(cueNumber.isNil,
      {cueList = cueList.add(function)},
      { 
        if(cueList.size < cueNumber) {
          cueList = cueList.extend(cueNumber, nil)
        }; 
        cueList[cueNumber - 1] = function;
      }
    );
    ^cueList.size;
  }

  setCurrent {arg cue;
    current = cue - 1;
    this.changed(\current);
  }

}
