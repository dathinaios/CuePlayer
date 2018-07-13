
CueTriggerCP : AbstractGUIComponentCP {

  var <trigButton, <cueNumberBox, <lrgCueWin, <largeCueNumberDisplay, <largeInfoTextField, <infoDisplayTextField;
  var <drawList, <frameRate = 24, drawRoutine;

  setDefaultOptions {
    super.setDefaultOptions;
    options.largeDisplay ?? { options.largeDisplay = false };
    options.largeDisplayBounds ?? { options.largeDisplayBounds = Rect(window.bounds.left - 670, 900, 600, 600)};
    options.infoDisplay ?? { options.infoDisplay = false };
    options.cueButtonFont ?? { options.cueButtonFont = Font("Lucida Grande", 12) };
    options.cueNumberBoxFont ?? { options.cueNumberBoxFont = Font("Lucida Grande", 22) };
  }

  createComponent {
    this.createCueTrigger;
  }

  createCueTrigger {
    this.createLabel("Trigger / Display & Reset Cue-number").align_(\left);
    this.createTriggerButton;
    this.createCueNumberBox;
    if (options.largeDisplay, { this.createLargeCueNumberDisplay });
    if (options.infoDisplay, { this.createInfoDisplay });
  }

  createTriggerButton {
    trigButton = Button(window, Rect(10, 200, 220, 60));
    trigButton.font_(options.cueButtonFont);
    trigButton.canFocus = false;
    trigButton.states_([["Trigger Next Cue", Color.white, Color.fromHexString("#1DA34D")]]);
    trigButton.action_(
      { "\nUse: \n \n cueTriggerCPInstance.trigButton.action = {}; \n \nto set this action to something useful!".postln; }
    );
  }

  createCueNumberBox {
    cueNumberBox = NumberBox(window, Rect(width: 50, height: 60)).align_(\center);
    cueNumberBox.font_(options.cueNumberBoxFont);
    cueNumberBox.action = { "\nUse: \n \n cueTriggerCPInstance.cueNumberBox.action = {}; \n \nto set this action to something useful!".postln; };
  }

  createInfoDisplay {
    infoDisplayTextField =  StaticText(window, Rect(width: 275, height: 40)).align_(\center);
    infoDisplayTextField.font_(Font(options.font.name, 12)).stringColor_(Color.white);
    infoDisplayTextField.background_(Color.fromHexString("#303030")); // 323232
  }

  createLargeCueNumberDisplay {var width, height;
    width = options.largeDisplayBounds.width;
    height = options.largeDisplayBounds.height;
    lrgCueWin = Window.new("", options.largeDisplayBounds, resizable: false).front;
    lrgCueWin.background = Color.black;

    largeCueNumberDisplay =  StaticText(lrgCueWin, Rect(width: width, height: height * 0.9)).align_(\center);
    largeCueNumberDisplay.font_(Font(options.font.name, width * 0.73)).stringColor_(Color.white);

	largeInfoTextField =  StaticText(lrgCueWin, Rect(0, (height * 0.9) -20, width: width, height: height*0.1)).align_(\center);
	largeInfoTextField.font_(Font(options.font.name, width * 0.06)).stringColor_(Color.white);
	largeInfoTextField.background_(Color.fromHexString("#303030"));

	lrgCueWin.front;

    this.setupAnimationForLrgWin;

  }

  setupAnimationForLrgWin {
    drawList = List.new;
    lrgCueWin.drawFunc = { drawList.do{arg i; i.value}; };
    drawRoutine = Routine({
      inf.do{
        this.refresh;
        (1/frameRate).wait;
      };
    });
  }

  clear {
    if (lrgCueWin.notNil and: {lrgCueWin.isClosed.not}) {lrgCueWin.close};
  }

  windowName {
    ^"Cue Trigger"
  }

  windowHeight {
	if( options.infoDisplay, { ^135 }, { ^95 });
  }

  setCurrent { arg cueNumber, cueObject;
    cueNumberBox.value = cueNumber;
    if (options.largeDisplay, {
	  largeCueNumberDisplay.string = cueNumber;
	  largeInfoTextField.string = cueObject.largeDisplayInfo;
	});
    if (options.infoDisplay, {
	  infoDisplayTextField.string = cueObject.cueTitle;
	});
  }

  runResources { }

  cmdPeriodAction {
    drawList.clear;
    drawRoutine.reset.play(AppClock);
  }

  addToDrawFunc { arg func;
    if( drawList.size == 0, {
      drawRoutine.reset.play(AppClock);
    });
    drawList.add(func);
  }

  removeFromDrawFunc { arg func;
    drawList.remove(func);
    if (drawList.size == 0){
      drawRoutine.stop;
      this.refresh;
    };
  }

  refresh {
    lrgCueWin.refresh;
  }

  lrgWinBounds {
    ^lrgCueWin.bounds;
  }

}