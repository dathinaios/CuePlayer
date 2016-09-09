
Cues { var <>cueList, <>current;
  
  *new {
    ^super.new.init;
  }

  init {
    current = 0;
    cueList = List.new;
  }

  next {
    cueList[current].value;
    current = current + 1;
    ^current;
  }

  trigger { arg cue = 1;
      this.setCurrent(cue - 1);
      ^this.next;
  }

  addCue { arg function;
    cueList.add(function);
    /* cuePatternFunction = {arg offset = 0; Pseq(list: cueList, repeats: inf, offset: offset).asStream;}; */
    /* cuePattern = cuePatternFunction.value; */
    ^cueList.size;
  }

  setCurrent {arg cue;
    current = cue - 1;
    this.changed(\current);
  }

}
