

AbstractGUIComponentCP { 

  var window, <options; 

  *new { arg window, options = ();
    ^super.newCopyArgs( window, options).init;
  }

  init {
    this.setDefaultOptions;
    this.runResources;
    this.createComponent;
    this.setCmdPeriodActions;
    window.front;
  }

  setDefaultOptions {
    window ?? {this.createMainWindow};
    options.font ?? {options.font = Font("Lucida Grande", 11)};
  }

  createMainWindow {
    window = Window(this.windowName, Rect(1400, 650, width: 282, height: this.windowHeight), resizable: false);
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    window.onClose = {
      this.clear;
    };
  }

  setCmdPeriodActions {
    CmdPeriod.add({
      AppClock.sched(0.1, {
        this.cmdPeriodAction;
      });
    });
  }

  createLabel { arg text = "placeholder text", width = 280, height = 20; var label;
    label = StaticText(window, Rect( width: width, height: height));
    label.font_(options.font);
    label.stringColor_(Color.fromHexString("#A0A0A0"););
    label.string_(text);
    ^label;
  }

  createComponent {
    this.subclassResponsibility(thisMethod);
  }

  runResources {
    this.subclassResponsibility(thisMethod);
  }

  cmdPeriodAction {
    this.subclassResponsibility(thisMethod);
  }

  clear {
    this.subclassResponsibility(thisMethod);
  }

  windowName {
    this.subclassResponsibility(thisMethod);
  }

  windowHeight {
    this.subclassResponsibility(thisMethod);
  }

}
