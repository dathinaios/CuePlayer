+ SequenceableCollection {

  asPluginSpecs { arg basePath; var items, specs;
    items = this.as(List);
    specs = List.new;
    if(items.size.odd) {
      "plugins must contain id/optionsOrPath pairs.".warn;
      ^List.new;
    };
    items.pairsDo({ arg id, optionsOrPath;
      var spec = id.asPluginSpec(optionsOrPath, basePath);
      if(specs.any({ arg existing; existing.id == spec.id })) {
        ("Plugin id % is already present in gui options; replacing.".format(spec.id)).warn;
        specs.removeAt(specs.detectIndex({ arg existing; existing.id == spec.id }));
      };
      specs.add(spec);
    });
    ^specs;
  }

}

+ Symbol {

  asPluginSpec { arg optionsOrPath, basePath;
    ^(id: this, options: optionsOrPath.asPluginOptions(basePath));
  }

}

+ String {

  asPluginSpec { arg optionsOrPath, basePath;
    ^this.asSymbol.asPluginSpec(optionsOrPath, basePath);
  }

  asPluginOptions { arg basePath;
    var path = this;
    if(path.beginsWith("/").not and:{ path.beginsWith("~").not }) {
      if(basePath.notNil) { path = PathName(basePath).pathOnly +/+ path };
    };
    ^path.standardizePath.load.asPluginOptions(basePath);
  }

}

+ Event {

  asPluginOptions { arg basePath; ^this }

}
