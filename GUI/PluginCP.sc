
PluginCP : AbstractGUIComponentCP {

  var <userState, <contentView;

  setDefaultOptions {
    super.setDefaultOptions;
    options.name      ?? { options.name = "Plugin" };
    options.height    ?? { options.height = 40 };
    options.showLabel ?? { options.showLabel = true };
    options[\build]       ?? { options[\build] = { arg plug; } };
    options[\onCue]       ?? { options[\onCue] = { arg plug, theChanged, message; } };
    options[\onCmdPeriod] ?? { options[\onCmdPeriod] = { arg plug; } };
    options[\onClear]     ?? { options[\onClear] = { arg plug; } };
  }

  createComponent {
    userState = IdentityDictionary.new;
    if(options.showLabel) { this.createLabel(options.name).align_(\left) };
    contentView = CompositeView(window, Rect(width: 280, height: options.height));
    contentView.background_(Color.fromHexString("#1E1E1E"));
    options[\build].value(this);
  }

  store { arg key, value; userState[key] = value; ^value }

  at { arg key; ^userState[key] }

  runResources { }

  cmdPeriodAction { options[\onCmdPeriod].value(this) }

  clear { options[\onClear].value(this) }

  update { arg theChanged, message;
    options[\onCue].value(this, theChanged, message);
  }

  windowName { ^options.name }

  windowHeight { ^options.height + if(options.showLabel, { 30 }, { 10 }) }

}
