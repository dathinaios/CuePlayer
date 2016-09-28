
Cues { 
  var <>cueList, <>current, >hook;
  var cueListRaw, <>liveReload = true;
  var attachmentList;

  *new {
    ^super.new.init;
  }

  init {
    current = 0;
    cueList = Array.new;
    cueListRaw = Array.new;
    attachmentList = Array.new;
  }

  add { arg function, attachment;
    cueList.add(function.asCueFunction);
    cueListRaw.add(function);
    attachmentList.add(attachment);
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

  next {
    hook.value(this);
    if(liveReload) {this.reloadCue};
    cueList[current].value;
    attachmentList[current].value;
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

  reloadCue {
    if(cueList[current].notNil){
      cueList[current] = cueListRaw[current].asCueFunction;
    }
  }

}
