
Cues { var <>cueList, current, cuePattern, cuePatternFunction;
  
  *new {
    ^super.new.init;
  }

  init {
    current = 0;
    cueList = List.new;
  }

  next {
    current = current + 1;
    cueList[current].value;
    ^current;
  }

  addCue { arg function;
    cueList.add(function);
    /* cuePatternFunction = {arg offset = 0; Pseq(list: cueList, repeats: inf, offset: offset).asStream;}; */
    /* cuePattern = cuePatternFunction.value; */
    ^cueList.size;
  }

  setCurrent {arg index;
    current = index - 1;
  }

}
