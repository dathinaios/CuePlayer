
CueTriggerCP : AbstractGUIComponentCP {

  var <trigButton, <cueNumberBox, lrgCueWin, <largeCueNumberDisplay, <largePerformerInfoDisplay, <infoDisplay;

  setDefaultOptions {
    super.setDefaultOptions;
    options.largeDisplay ?? { options.largeDisplay = false };
    options.largeDisplayBounds ?? { options.largeDisplayBounds = Rect(window.bounds.left - 970, 900, 950, 950)};
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
    infoDisplay =  StaticText(window, Rect(width: 275, height: 60)).align_(\center);
    infoDisplay.font_(Font(options.font.name, 15)).stringColor_(Color.white);
    infoDisplay.background_(Color.black);
  }

  createLargeCueNumberDisplay {var width, height;
    width = options.largeDisplayBounds.width;
    height = options.largeDisplayBounds.height;
    lrgCueWin = Window.new("Performer Info", options.largeDisplayBounds, resizable: false).front;
    lrgCueWin.background = Color.black;

    largeCueNumberDisplay =  StaticText(lrgCueWin, Rect(width: width, height: height)).align_(\center);
    largeCueNumberDisplay.font_(Font(options.font.name, width * 0.73)).stringColor_(Color.white);

    largePerformerInfoDisplay =  StaticText(lrgCueWin, Rect(0, 30,width: width, height: height*0.05)).align_(\center);
    largePerformerInfoDisplay.font_(Font(options.font.name, width * 0.03)).stringColor_(Color.white);

    lrgCueWin.front;
  }

  clear {
    if (lrgCueWin.notNil and: {lrgCueWin.isClosed.not}) {lrgCueWin.close};
  }

  windowName {
    ^"Cue Trigger"
  }

  windowHeight {
	if( options.infoDisplay, { ^155 }, { ^95 });
  }

  setCurrent { arg cueNumber, cueObject;
    cueNumberBox.value = cueNumber;
    if (options.largeDisplay, {
	  largeCueNumberDisplay.string = cueNumber;
	  largePerformerInfoDisplay.string = cueObject.performerInfo;
	});
    if (options.infoDisplay, {
	  infoDisplay.string = cueObject.info;
	});
  }

  runResources { }

  cmdPeriodAction { }

}