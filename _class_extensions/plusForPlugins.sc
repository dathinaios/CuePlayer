+ SequenceableCollection {

  asPluginSpecs { arg basePath; var specs;
    specs = List.new;
    this.do({ arg item;
      var spec = item.asPluginSpec(basePath);
      spec.notNil.if {
        if(specs.any({ arg ex; ex.id == spec.id })) {
          ("Plugin id % is already present in gui options; replacing.".format(spec.id)).warn;
          specs.removeAt(specs.detectIndex({ arg ex; ex.id == spec.id }));
        };
        specs.add(spec);
      };
    });
    ^specs;
  }

}

+ Symbol {

  asPluginSpec { arg basePath;
    var pluginsDir, path;
    pluginsDir = PathName(CuePlayer.filenameSymbol.asString).pathOnly +/+ "Plugins";
    path = pluginsDir +/+ (this.asString ++ ".scd");
    if(File.exists(path).not) {
      ("Built-in plugin '%' not found at %.".format(this, path)).warn;
      ^nil;
    };
    ^(id: this, options: path.load.asPluginOptions(nil));
  }

}

+ String {

  asPluginSpec { arg basePath;
    var id = PathName(this).fileNameWithoutExtension.asSymbol;
    ^(id: id, options: this.asPluginOptions(basePath));
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
