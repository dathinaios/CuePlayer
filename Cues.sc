
Cues {
  var <>cueList, <>current, >hook;
  var cueListRaw, <>liveReload = true;
  var attachmentList;
  var blockTrigger;

  *new {
    ^super.new.init;
  }

  init {
    current = 0;
    cueList = Array.new;
    cueListRaw = Array.new;
    attachmentList = Array.new;
    blockTrigger = BlockTriggerCP.new;
  }

  add { arg function, attachment;
    cueList = cueList.add(function.asCueFunction);
    cueListRaw = cueListRaw.add(function);
    attachmentList = attachmentList.add(attachment);
  }

  put {arg cueNumber, function, attachment;
    if(cueList.size < cueNumber) {
      cueList = cueList.extend(cueNumber, nil);
      cueListRaw = cueListRaw.extend(cueNumber, nil);
      attachmentList = attachmentList.extend(cueNumber, nil);
    };
    cueListRaw[cueNumber - 1] = function;
    attachmentList[cueNumber - 1] = attachment;
    cueList[cueNumber - 1] = function.asCueFunction;
  }

  next { var currentFuncIndex;
    currentFuncIndex = current;
    if(blockTrigger.allow){
      hook.value(this);
      current = current + 1;
      if(liveReload) {this.reloadCue};
      cueList[currentFuncIndex].value(this);
      attachmentList[currentFuncIndex].value(this);
      this.changed(\current);
      ^current;
    }
  }

  trigger { arg cueNumber = 1;
    cueNumber = cueNumber - 1;
    this.setCurrent(cueNumber);
    ^this.next;
  }

  setCurrent {arg cueNumber;
    /*("Next cue will be " ++ (cue + 1)).postln;*/
    current = cueNumber.abs;
    this.changed(\current);
    ^current;
  }

  getCueObject { arg cueNumber;
      ^cueList[cueNumber - 1];
  }

  reloadCue {
    if(cueList[current].notNil){
      cueList[current] = cueListRaw[current].asCueFunction;
    }
  }

  blockTrigger { arg interval = 0.3;
    blockTrigger.interval = interval;
  }

}
