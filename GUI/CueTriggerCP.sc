
CueTriggerCP : AbstractGUIComponentCP {

  var <trigButton, <cueNumberBox, <infoDisplayTextField;

  setDefaultOptions {
    super.setDefaultOptions;
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

  clear { }

  windowName {
    ^"Cue Trigger"
  }

  windowHeight {
	if( options.infoDisplay, { ^135 }, { ^95 });
  }

  setCurrent { arg cueNumber, cueObject;
    cueNumberBox.value = cueNumber;
    if (options.infoDisplay, {
	  infoDisplayTextField.string = cueObject.cueTitle;
	});
  }

  runResources { }

  cmdPeriodAction { }

}
