
LargeDisplayCP : AbstractGUIComponentCP {

  var <lrgCueWin, <largeCueNumberDisplay, <largeInfoTextField;
  var <drawList, <frameRate = 24, drawRoutine;

  setDefaultOptions {
    super.setDefaultOptions;
    options.largeDisplayBounds ?? { options.largeDisplayBounds = Rect(window.bounds.left - 670, 900, 600, 600) };
  }

  createComponent { var bounds, w, h;
    bounds = options.largeDisplayBounds;
    w = bounds.width;
    h = bounds.height;

    lrgCueWin = Window.new("", bounds, resizable: false);
    lrgCueWin.background = Color.black;

    largeCueNumberDisplay = StaticText(lrgCueWin, Rect(width: w, height: h * 0.9)).align_(\center);
    largeCueNumberDisplay.font_(Font(options.font.name, w * 0.73)).stringColor_(Color.white);

    largeInfoTextField = StaticText(lrgCueWin, Rect(0, (h * 0.9) - 20, width: w, height: h * 0.1)).align_(\center);
    largeInfoTextField.font_(Font(options.font.name, w * 0.06)).stringColor_(Color.white);
    largeInfoTextField.background_(Color.fromHexString("#303030"));

    lrgCueWin.front;
    this.setupAnimation;
  }

  setupAnimation {
    drawList = List.new;
    lrgCueWin.drawFunc = { drawList.do{arg i; i.value} };
    drawRoutine = Routine({
      inf.do {
        this.refresh;
        (1/frameRate).wait;
      };
    });
  }

  runResources { }

  setCurrent { arg cueNumber, cueObject;
    largeCueNumberDisplay.string = cueNumber;
    largeInfoTextField.string = cueObject.largeDisplayInfo;
  }

  addToDrawFunc { arg func;
    if(drawList.size == 0) { drawRoutine.reset.play(AppClock) };
    drawList.add(func);
  }

  removeFromDrawFunc { arg func;
    drawList.remove(func);
    if(drawList.size == 0) { drawRoutine.stop; this.refresh };
  }

  refresh {
    lrgCueWin.refresh;
  }

  lrgWinBounds {
    ^lrgCueWin.bounds;
  }

  clear {
    if(lrgCueWin.notNil and: { lrgCueWin.isClosed.not }) { lrgCueWin.close };
  }

  cmdPeriodAction {
    drawList.clear;
    drawRoutine.stop;
    this.refresh;
  }

  windowHeight { ^0 }

  windowName { ^"Large Display" }

}
