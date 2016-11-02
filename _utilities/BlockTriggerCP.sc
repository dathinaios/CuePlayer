
BlockTriggerCP{ 
  var <>interval;
  var list, pattern;

  *new {
    ^super.new.init;
  }

  init {
    list = List[0,0];
    pattern = Pseq([0, 1], inf ).asStream;
  }

  allow {
    if(interval.notNil, {
      list[pattern.next] = Main.elapsedTime;
      ^(list[0] - list[1]).abs > interval;
    }, { ^true });
  }

}

