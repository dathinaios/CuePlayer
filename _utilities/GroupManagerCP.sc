
GroupManagerCP { classvar <groupA, <groupB, <groupZ;

  *initialise { var makeGroupsFunction;
    if(groupA.isNil && groupB.isNil && groupZ.isNil, {
      makeGroupsFunction = {
        groupA = Group.before(Server.default);
        groupB = Group.after(groupA);
        groupZ = Group.after(Server.default);
      };
      makeGroupsFunction.value;
      Server.default.tree = {makeGroupsFunction.value}
    }, { ^"GroupManagerCP is already initialised."});
  }

}
